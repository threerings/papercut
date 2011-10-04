//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import pythagoras.f.IPoint;
import pythagoras.f.Rectangle;

import react.Slot;
import react.UnitSlot;
import react.ValueView;
import react.Values;

import playn.core.Font;
import playn.core.ImageLayer;
import playn.core.Json;
import playn.core.Layer;
import playn.core.Mouse;
import playn.core.PlayN;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Interface;
import tripleplay.ui.Root;
import tripleplay.ui.Selector;
import tripleplay.ui.Stylesheet;
import tripleplay.util.Input;
import tripleplay.util.MouseInput;

import flashbang.AppMode;
import flashbang.anim.Movie;
import flashbang.anim.rsrc.EditableAnimConf;
import flashbang.anim.rsrc.EditableMovieConf;
import flashbang.anim.rsrc.EditableMovieImageLayerConf;
import flashbang.anim.rsrc.KeyframeType;
import flashbang.rsrc.ImageResource;
import flashbang.rsrc.JsonResource;

import static papercut.PapercutApp.SCREEN_SIZE;

import static tripleplay.ui.Style.*;
import static tripleplay.ui.Styles.make;

public class AnimateMode extends AppMode
{
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

        Root listingRoot = _iface.createRoot(AxisLayout.vertical().gap(0), ROOT, modeLayer).
            setStyles(make(HALIGN.left, VALIGN.top)).setSize(LISTING_WIDTH, LISTING_HEIGHT);
        for (String image : _images) {
            listingRoot.add(new Button().setText(image));
        }
        _selector = new Selector(listingRoot, listingRoot.childAt(0));

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
            setBounds(LISTING_WIDTH + STAGE_WIDTH, 0, LISTING_WIDTH, LISTING_HEIGHT).
            add(_editor, playToggle, save);

        _iface.createRoot(AxisLayout.vertical(), ROOT, modeLayer).
            setStyles(make(VALIGN.top)).setBounds(0, LISTING_HEIGHT, SCREEN_SIZE.x(), TREE_HEIGHT).
            add(_layerTree, new LayerEditor(_movieConf, _layerTree));
        _layerTree.frameSelected.connect(new UnitSlot () {
            @Override public void onEmit () {
                if (_movie  == null) return;
                _editor.setFrame(_layerTree.frame(), _layerTree.anim());
                _movie.setFrame(_layerTree.frame());
            }
        });

        PlayN.mouse().setListener(_minput.mlistener);

        Input.Region stageRegion =
            new Input.BoundsRegion(new Rectangle(LISTING_WIDTH, 0, STAGE_WIDTH, LISTING_HEIGHT));
        _minput.register(stageRegion, new Mouse.Adapter() {
            @Override public void onMouseMove (Mouse.MotionEvent ev) {
                if (_image == null) {
                    ImageResource rsrc = ImageResource.require(imageName());
                    _image = PlayN.graphics().createImageLayer(rsrc.image());
                    modeLayer.add(_image);
                }
                _image.setTranslation(ev.x(), ev.y());
            }

            @Override public void onMouseUp (Mouse.ButtonEvent ev) {
                EditableMovieImageLayerConf desc = new EditableMovieImageLayerConf();
                desc.name.update(imageName());

                EditableAnimConf layerAnim = new EditableAnimConf();
                layerAnim.keyframes.get(KeyframeType.X_LOCATION).value.update(
                    ev.x() - LISTING_WIDTH);
                layerAnim.keyframes.get(KeyframeType.Y_LOCATION).value.update(ev.y());
                desc.animations.put(_movieConf.animation.get(), layerAnim);
                _movieConf.add(_layerTree.groupLayer(), desc);
            }
        });

        _minput.register(new NotRegion(stageRegion), new Mouse.Adapter() {
            @Override public void onMouseMove (Mouse.MotionEvent ev) {
                if (_image != null) {
                    _image.destroy();
                    _image = null;
                }
            }
        });
        play();
    }

    protected String imageName () {
        return ((Button)_selector.selected.get()).text();
    }

    protected void play () {
        if (_movie != null) {
            _movie.destroySelf();
            _movie = null;
        }
        if (_movieConf.root.children.isEmpty()) return;

        _movie = _movieConf.build();
        _movie.layer().setTranslation(LISTING_WIDTH, 0);
        _movie.setStopped(!_playing.get());
        _movie.setFrame(_layerTree.frame());
        addObject(_movie, modeLayer);
    }

    @Override public void update (float dt) {
        super.update(dt);
        _iface.paint(0);
    }

    public static class NotRegion extends Input.Region {
        public NotRegion (Input.Region region) {
            _region = region;
        }

        @Override public boolean hitTest (IPoint p) {
            return !_region.hitTest(p);
        }

        protected final Input.Region _region;
    }

    protected Movie _movie;
    protected Interface _iface;
    protected ImageLayer _image;
    protected Selector _selector;
    protected ValueView<Boolean> _playing;

    protected final EditableMovieConf _movieConf;
    protected final LayerTree _layerTree;
    protected final KeyframeEditor _editor = new KeyframeEditor();
    protected final MouseInput _minput = new MouseInput();
    protected final Iterable<String> _images;

    protected static final Font SMALL =
        PlayN.graphics().createFont("Helvetica", Font.Style.PLAIN, 12);

    protected static final Stylesheet ROOT = Stylesheet.builder().
        add(Element.class, make(FONT.is(SMALL))).
        add(Button.class,
            make(BACKGROUND.is(Background.solid(0xFFFFFFFF, 2))).
            addSelected(COLOR.is(0xFFFFFFFF), BACKGROUND.is(Background.solid(0xFF000000, 2)))).
        create();

    protected static final int LISTING_WIDTH = 200, LISTING_HEIGHT = 400;
    protected static final int TREE_HEIGHT = SCREEN_SIZE.y() - LISTING_HEIGHT;
    protected static final int STAGE_WIDTH = SCREEN_SIZE.x() - LISTING_WIDTH * 2;
}
