//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.Font;
import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.Mouse;
import playn.core.PlayN;

import pythagoras.f.IPoint;
import pythagoras.f.Rectangle;

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
import tripleplay.ui.Selector;
import tripleplay.ui.Stylesheet;
import tripleplay.util.Input;
import tripleplay.util.MouseInput;

import flashbang.AppMode;
import flashbang.anim.AnimationController;
import flashbang.anim.Model;
import flashbang.anim.rsrc.EditableLayerAnimation;
import flashbang.anim.rsrc.EditableModelImageLayer;
import flashbang.anim.rsrc.EditableModelResource;
import flashbang.anim.rsrc.Keyframe;
import flashbang.anim.rsrc.KeyframeType;
import flashbang.rsrc.ImageResource;

import static papercut.PapercutApp.SCREEN_SIZE;

import static tripleplay.ui.Style.*;
import static tripleplay.ui.Styles.make;

public class AnimateMode extends AppMode
{
    public AnimateMode (Iterable<String> images) {
        _images = images;
        _model.animations.add("default");
        _model.animation.update("default");
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
        _selector = new Selector().add(listingRoot).setSelected(listingRoot.childAt(0));

        _editor.edited.connect(new UnitSlot() {
            @Override public void onEmit () {
                play();
            }
        });

        final Button playToggle = new Button("Play");
        _playing = Values.toggler(playToggle.clicked(), false);
        _playing.connect(new Slot<Boolean> () {
            @Override public void onEmit (Boolean play) {
                playToggle.setText(play ? "Stop" : "Play");
                if (_anim == null) return;
                play();
            }
        });
        _iface.createRoot(AxisLayout.vertical(), ROOT, modeLayer).
            setStyles(make(VALIGN.top)).
            setBounds(LISTING_WIDTH + STAGE_WIDTH, 0, LISTING_WIDTH, LISTING_HEIGHT).
            add(_editor, playToggle);

        _iface.createRoot(AxisLayout.vertical().gap(0), ROOT, modeLayer).
            setStyles(make(VALIGN.top)).setBounds(0, LISTING_HEIGHT, SCREEN_SIZE.x(), TREE_HEIGHT).
            add(_layerTree);
        _layerTree.frameSelected.connect(new UnitSlot () {
            @Override public void onEmit () {
                if (_anim  == null) return;
                _editor.setFrame(_layerTree.frame(), _layerTree.layer());
                _anim.setFrame(_layerTree.frame());
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
                EditableModelImageLayer desc = new EditableModelImageLayer();
                desc.imagePath.update(imageName());
                desc.name.update(imageName());

                EditableLayerAnimation layerAnim = new EditableLayerAnimation();
                layerAnim.keyframes.get(KeyframeType.X_LOCATION).value.update(
                    ev.x() - LISTING_WIDTH);
                layerAnim.keyframes.get(KeyframeType.Y_LOCATION).value.update(ev.y());
                desc.animations.put("default", layerAnim);
                _model.children.add(desc);

                play();
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
    }

    protected String imageName () {
        return ((Button)_selector.selected()).text();
    }

    protected void play () {
        if (_displayed != null) {
            _displayed.destroySelf();
        }

        _displayed = _model.build();
        _displayed.layer().setTranslation(LISTING_WIDTH, 0);
        _anim = _displayed.play("default");
        _anim.setStopped(!_playing.get());
        _anim.setFrame(_layerTree.frame());
        addObject(_displayed, modeLayer);
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

    protected Model _displayed;
    protected AnimationController _anim;
    protected Interface _iface;
    protected ImageLayer _image;
    protected Selector _selector;
    protected ValueView<Boolean> _playing;

    protected final EditableModelResource _model = new EditableModelResource();
    protected final LayerTree _layerTree = new LayerTree(_model);
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
