//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.Pointer;
import playn.core.SurfaceLayer;
import playn.core.Graphics;
import java.util.List;

import com.google.common.collect.Lists;

import playn.core.Font;
import playn.core.GroupLayer;
import playn.core.Json;
import playn.core.Layer;
import playn.core.PlayN;

import pythagoras.f.Point;

import react.Slot;
import react.UnitSlot;
import react.ValueView;
import react.Values;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Field;
import tripleplay.ui.Interface;
import tripleplay.ui.Root;
import tripleplay.ui.Selector;
import tripleplay.ui.Stylesheet;

import flashbang.AppMode;
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

    public static final Stylesheet ROOT = Stylesheet.builder().
        add(Element.class, make(FONT.is(SMALL))).
        add(Button.class,
                make(BACKGROUND.is(Background.solid(0xFFFFFFFF, 2))).
                addSelected(COLOR.is(0xFFFFFFFF), BACKGROUND.is(Background.solid(0xFF000000, 2)))).
        create();

    public static Root popup (final Interface iface, GroupLayer parent, Iterable<String> choices,
            final Slot<String> onSelected) {
        final Root root = iface.createRoot(AxisLayout.vertical().gap(0), ROOT, parent);
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
        root.pack();
        new Selector().add(root).selected.connect(new Slot<Element<?>> () {
            @Override public void onEmit (Element<?> emitted) {
                String selected = ((Button)emitted).text.get();
                if (!selected.equals("Cancel")) onSelected.onEmit(selected);
                iface.destroyRoot(root);
            }
        });
        return root;
    }

    public AnimateMode (Iterable<String> images) {
        _images = images;
        Json.Object root = JsonResource.require("streetwalker/streetwalker.json").json();
        _movieConf = new EditableMovieConf(root.getArray("children", Json.Object.class));
        _layerTree = new LayerTree(_movieConf);
    }

    @Override protected void setup () {
        super.setup();

        PlayN.keyboard().setListener(iface.klistener);

        UnitSlot playSlot = new UnitSlot() {
            @Override public void onEmit () {
                play();
            }
        };
        _editor = new KeyframeEditor(iface);
        _editor.edited.connect(playSlot);
        _movieConf.treeChanged.connect(playSlot);

        final Button playToggle = new Button("Play");
        _playing = Values.toggler(playToggle.clicked(), false);
        _playing.connect(new Slot<Boolean> () {
            @Override public void onEmit (Boolean play) {
                playToggle.text.update(play ? "Stop" : "Play");
                if (_movie == null) return;
                play();
            }
        });

        Button save = new Button("Save");
        save.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                Papercut.write("streetwalker/streetwalker.json", _movieConf.write());
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
                // Translate our list to the top; it's too long to fit
                Point screen0 = Layer.Util.layerToScreen(pop.layer, 0, 0);
                pop.layer.setTranslation(pop.layer.transform().tx(), -screen0.y());
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
        play();
    }

    protected void play () {
        if (_movie != null) {
            _movie.destroySelf();
            _movie = null;
        }
        if (_movieConf.root.children.isEmpty()) return;

        _movie = _movieConf.build();
        _movie.setStopped(!_playing.get());
        _movie.setFrame(_layerTree.frame());
        _movie.setLoc(STAGE_WIDTH/2, STAGE_HEIGHT/2);
        addObject(_movie, modeLayer);
        SurfaceLayer surface = PlayN.graphics().createSurfaceLayer(10, 10);
        surface.surface().setFillColor(0xFF000000).drawLine(0, 5, 10, 5, 1).drawLine(5, 0, 5, 10, 1);
        surface.setTranslation(STAGE_WIDTH/2 - 5, STAGE_HEIGHT/2 - 5);
        modeLayer.add(surface);
    }

    protected Movie _movie;
    protected ValueView<Boolean> _playing;

    protected final EditableMovieConf _movieConf;
    protected final LayerTree _layerTree;
    protected KeyframeEditor _editor;
    protected final Iterable<String> _images;

    protected static final int EDITOR_WIDTH = 300, EDITOR_HEIGHT = 400;
    protected static final int TREE_HEIGHT = SCREEN_SIZE.y() - EDITOR_HEIGHT;
    protected static final int STAGE_WIDTH = SCREEN_SIZE.x() - EDITOR_WIDTH;
    protected static final int STAGE_HEIGHT = SCREEN_SIZE.y() - TREE_HEIGHT;
}
