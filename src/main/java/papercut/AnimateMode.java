//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.Font;
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
import tripleplay.ui.Interface;
import tripleplay.ui.Root;
import tripleplay.ui.Stylesheet;

import flashbang.AppMode;
import flashbang.anim.Movie;
import flashbang.anim.rsrc.EditableMovieConf;
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

    public AnimateMode (Iterable<String> images) {
        _images = images;
        Json.Object root = JsonResource.require("streetwalker/streetwalker.json").json();
        _movieConf = new EditableMovieConf(root.getArray("movies", Json.Object.class).get(0));
        _layerTree = new LayerTree(_movieConf);
    }

    @Override protected void setup () {
        super.setup();

        _iface = new Interface(pointerListener());
        PlayN.pointer().setListener(_iface.plistener);

        UnitSlot playSlot = new UnitSlot() {
            @Override public void onEmit () {
                play();
            }
        };
        _editor.edited.connect(playSlot);
        _movieConf.treeChanged.connect(playSlot);

        final Button playToggle = new Button("Play");
        _playing = Values.toggler(playToggle.clicked(), false);
        _playing.connect(new Slot<Boolean> () {
            @Override public void onEmit (Boolean play) {
                playToggle.setText(play ? "Stop" : "Play");
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
        _iface.createRoot(AxisLayout.vertical(), ROOT, modeLayer).
            setStyles(make(VALIGN.top)).
            setBounds(STAGE_WIDTH, 0, EDITOR_WIDTH, EDITOR_HEIGHT).
            add(_editor, playToggle, save);

        Slot<Button> onAdd = new Slot<Button> () {
            @Override public void onEmit (Button clicked) {
                Point button = Layer.Util.layerToScreen(clicked.layer, 0, 0);
                new LayerCreator(_iface, modeLayer, _images, _movieConf, _layerTree, button.x, button.y);
            }
        };
        _iface.createRoot(AxisLayout.vertical(), ROOT, modeLayer).
            setStyles(make(VALIGN.top)).setBounds(0, EDITOR_HEIGHT, SCREEN_SIZE.x(), TREE_HEIGHT).
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
        addObject(_movie, modeLayer);
    }

    @Override public void update (float dt) {
        super.update(dt);
        _iface.paint(0);
    }

    protected Movie _movie;
    protected Interface _iface;
    protected ValueView<Boolean> _playing;

    protected final EditableMovieConf _movieConf;
    protected final LayerTree _layerTree;
    protected final KeyframeEditor _editor = new KeyframeEditor();
    protected final Iterable<String> _images;

    protected static final int EDITOR_WIDTH = 200, EDITOR_HEIGHT = 400;
    protected static final int TREE_HEIGHT = SCREEN_SIZE.y() - EDITOR_HEIGHT;
    protected static final int STAGE_WIDTH = SCREEN_SIZE.x() - EDITOR_WIDTH;
}
