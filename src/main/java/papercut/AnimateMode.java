//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import java.util.List;

import com.google.common.collect.Lists;

import playn.core.Font;
import playn.core.GroupLayer;
import playn.core.Json;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.SurfaceLayer;

import pythagoras.f.Point;

import react.Slot;
import react.UnitSlot;
import react.Value;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Field;
import tripleplay.ui.Interface;
import tripleplay.ui.Root;
import tripleplay.ui.Selector;
import tripleplay.ui.SimpleStyles;
import tripleplay.ui.Stylesheet;

import flashbang.AppMode;
import flashbang.Flashbang;
import flashbang.anim.Movie;
import flashbang.anim.rsrc.EditableMovieConf;
import flashbang.anim.rsrc.EditableMovieGroupLayerConf;
import flashbang.anim.rsrc.EditableMovieImageLayerConf;
import flashbang.rsrc.JsonResource;

import static papercut.PapercutApp.SCREEN_SIZE;

import static tripleplay.ui.Style.*;
import static tripleplay.ui.Styles.make;

public class AnimateMode extends AppMode
{
    protected static final Font SMALL =
        PlayN.graphics().createFont("Helvetica", Font.Style.PLAIN, 12);

    public static final Stylesheet ROOT = SimpleStyles.newSheetBuilder().
        add(Element.class, make(FONT.is(SMALL))).
        create();

    public static Root popup (final Interface iface, GroupLayer parent, Iterable<String> choices,
            final Slot<String> onSelected) {
        final Root root = iface.createRoot(AxisLayout.vertical().gap(0), ROOT,
            Flashbang.mode().modeLayer);
        // Dismiss the popup if the user clicks outside of it
        root.setPointerDelegate(new Root.PointerDelegate() {
            @Override public boolean handlePointerStart(Pointer.Event event) { return true; }
            @Override public void onPointerDrag(Pointer.Event event) { /* NOOP! */ }
            @Override public void onPointerEnd(Pointer.Event event) { iface.destroyRoot(root); }
        });
        for (String choice : choices) {
            root.add(new Button(choice));
        }
        root.add(new Button("Cancel"));
        Point corner = Layer.Util.layerToScreen(parent, 0, 0);
        root.pack().layer.setTranslation(corner.x, corner.y);
        new Selector().add(root).selected.connect(new Slot<Element<?>> () {
            @Override public void onEmit (Element<?> emitted) {
                String selected = ((Button)emitted).text.get();
                if (!selected.equals("Cancel")) onSelected.onEmit(selected);
                iface.destroyRoot(root);
            }
        });
        return root;
    }

    public final Value<Boolean> playing = Value.create(false);

    public AnimateMode (Iterable<String> images, LocalAssets assets) {
        _images = images;
        _assets = assets;
        Json.Object root = JsonResource.require("streetwalker/streetwalker.json").json();
        _movieConf = new EditableMovieConf(root.getArray("children", Json.Object.class));
        _layerTree = new LayerTree(_movieConf);
    }

    @Override protected void setup () {
        super.setup();

        PlayN.keyboard().setListener(iface.klistener);

        UnitSlot updateSlot = new UnitSlot() {
            @Override public void onEmit () {
                updateMovie();
            }
        };
        _editor = new KeyframeEditor(iface);
        _editor.edited.connect(updateSlot);
        _movieConf.treeChanged.connect(updateSlot);

        final Button playToggle = new Button("Play");
        playToggle.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                playing.update(!playing.get());
            }
        });
        playing.connect(new Slot<Boolean> () {
            @Override public void onEmit (Boolean play) {
                playToggle.text.update(play ? "Stop" : "Play");
                if (_movie == null) return;
                updateMovie();
            }
        });

        Button save = new Button("Save");
        save.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                _assets.write("streetwalker/streetwalker.json", _movieConf.write());
            }
        });

        final Field frame = new Field("0");
        _layerTree.frameSelected.connect(new UnitSlot() {
            @Override public void onEmit () {
                frame.text.update("" + _layerTree.frame());
            }
        });
        frame.defocused.connect(new UnitSlot() {
            @Override public void onEmit () {
                try {
                    _layerTree.setFrame(Integer.parseInt(frame.text.get()));
                } catch (NumberFormatException e) {}
            }
        });


        iface.createRoot(AxisLayout.vertical(), ROOT, modeLayer).
            setStyles(make(VALIGN.top)).
            setBounds(STAGE_WIDTH, 0, EDITOR_WIDTH, EDITOR_HEIGHT).
            add(_editor, playToggle, save, frame);

        Slot<Button> onAdd = new Slot<Button> () {
            @Override public void onEmit (Button clicked) {
                List<String> choices = Lists.newArrayList(_images);
                choices.add("New Group");
                Root pop = popup(iface, clicked.layer, choices, new Slot<String> () {
                    @Override public void onEmit (String selected) {
                        if (selected.equals("New Group")) {
                            _movieConf.add(_layerTree.groupLayer(),
                                new EditableMovieGroupLayerConf("Group"));
                        } else {
                            _movieConf.add(_layerTree.groupLayer(),
                                new EditableMovieImageLayerConf(selected));
                        }
                    }
                });
            }
        };
        iface.createRoot(AxisLayout.vertical().offPolicy(AxisLayout.Policy.STRETCH), ROOT, modeLayer).
            setStyles(make(VALIGN.top)).
            setBounds(5, EDITOR_HEIGHT, SCREEN_SIZE.x() - 10, TREE_HEIGHT).
            add(_layerTree, new LayerEditor(_movieConf, _layerTree, onAdd));
        _layerTree.frameSelected.connect(new UnitSlot () {
            @Override public void onEmit () {
                if (_movie  == null) return;
                _editor.setFrame(_layerTree.frame(), _layerTree.anim());
                _movie.setFrame(_layerTree.frame());
            }
        });
        updateMovie();
    }

    protected void updateMovie () {
        if (_movie != null) {
            _movie.destroySelf();
            _movie = null;
        }
        if (_movieConf.root.children.isEmpty()) return;

        _movie = _movieConf.build();
        _movie.setStopped(!playing.get());
        _movie.setFrame(_layerTree.frame());
        _movie.setLoc(STAGE_WIDTH/2, STAGE_HEIGHT/2);
        addObject(_movie, modeLayer);
        SurfaceLayer surface = PlayN.graphics().createSurfaceLayer(10, 10);
        surface.surface().setFillColor(0xFF000000).drawLine(0, 5, 10, 5, 1).drawLine(5, 0, 5, 10, 1);
        surface.setTranslation(STAGE_WIDTH/2 - 5, STAGE_HEIGHT/2 - 5);
        modeLayer.add(surface);
    }

    public void stop () {
    }

    protected Movie _movie;

    protected final EditableMovieConf _movieConf;
    protected final LayerTree _layerTree;
    protected KeyframeEditor _editor;
    protected final Iterable<String> _images;
    protected final LocalAssets _assets;

    protected static final int EDITOR_WIDTH = 300, EDITOR_HEIGHT = 400;
    protected static final int TREE_HEIGHT = SCREEN_SIZE.y() - EDITOR_HEIGHT;
    protected static final int STAGE_WIDTH = SCREEN_SIZE.x() - EDITOR_WIDTH;
    protected static final int STAGE_HEIGHT = SCREEN_SIZE.y() - TREE_HEIGHT;
}
