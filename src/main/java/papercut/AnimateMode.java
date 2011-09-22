//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import pythagoras.f.IPoint;
import pythagoras.f.Rectangle;

import react.Slot;

import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.Mouse;
import playn.core.PlayN;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Interface;
import tripleplay.ui.Root;
import tripleplay.ui.Selector;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.Stylesheet;
import tripleplay.util.Input;
import tripleplay.util.MouseInput;

import flashbang.AppMode;
import flashbang.anim.Model;
import flashbang.anim.rsrc.EditableLayerAnimation;
import flashbang.anim.rsrc.EditableModelAnimation;
import flashbang.anim.rsrc.EditableModelResource;
import flashbang.anim.rsrc.ImageLayerDesc;
import flashbang.anim.rsrc.Keyframe;
import flashbang.anim.rsrc.KeyframeType;
import flashbang.anim.rsrc.ModelResource;
import flashbang.rsrc.ImageResource;

import static papercut.PapercutApp.SCREEN_SIZE;

public class AnimateMode extends AppMode
{
    public AnimateMode (Iterable<String> images) {
        _images = images;
        _model.animations.put("default", new EditableModelAnimation());
    }

    @Override protected void setup () {
        super.setup();

        _iface = new Interface(pointerListener());
        PlayN.pointer().setListener(_iface.plistener);

        Styles buttonStyles = Styles.none().
            add(
                Style.HALIGN.is(Style.HAlign.LEFT),
                Style.BACKGROUND.is(Background.solid(0xFFFFFFFF, 2))).
            addSelected(
                Style.COLOR.is(0xFFFFFFFF), Style.BACKGROUND.is(Background.solid(0xFF000000, 2)));
        _listing = _iface.createRoot(AxisLayout.vertical().gap(0),
            Stylesheet.builder().add(Button.class, buttonStyles).create());
        modeLayer.add(_listing.layer);
        for (String image : _images) {
            _listing.add(new Button().setText(image));
        }
        _selector = new Selector(_listing).setSelected(_listing.childAt(0));
        _listing.setSize(LISTING_WIDTH, LISTING_HEIGHT);

        _editor = _iface.createRoot(AxisLayout.horizontal());
        modeLayer.add(_editor.layer);
        _editor.layer.setTranslation(LISTING_WIDTH + STAGE_WIDTH, 0);
        final KeyframeEditor editor = new KeyframeEditor();
        _editor.add(editor);
        _editor.setSize(LISTING_WIDTH, LISTING_HEIGHT);

        _tree = _iface.createRoot(AxisLayout.vertical().gap(0));
        modeLayer.add(_tree.layer);
        _tree.layer.setTranslation(0, LISTING_HEIGHT);
        LayerTree lt = new LayerTree(_model, _model.animations.get("default"));
        _tree.add(lt);
        lt.frameSelected.connect(new Slot<Integer> () {
            @Override public void onEmit (Integer frame) {
                editor.setFrame(frame);
            }
        });
        _tree.setSize(SCREEN_SIZE.x(), TREE_HEIGHT);

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
                if (_displayed != null) {
                    _displayed.destroySelf();
                }
                ImageLayerDesc desc = new ImageLayerDesc();
                desc.imageName = desc.name = imageName();
                _model.layers.add(desc);
                EditableModelAnimation anim = _model.animations.get("default");
                EditableLayerAnimation layerAnim = new EditableLayerAnimation(desc.imageName);
                anim.layers.add(layerAnim);
                layerAnim.keyframes.get(KeyframeType.X_LOCATION).value.update(
                    ev.x() - _listing.size().width());
                layerAnim.keyframes.get(KeyframeType.Y_LOCATION).value.update(ev.y());
                editor._layer = layerAnim;

                _displayed = new Model(_model);
                _displayed.layer().setTranslation(_listing.size().width(), 0);
                _displayed.playAnimation("default");
                addObject(_displayed, modeLayer);
            }

            protected Model _displayed;
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

    protected Root _listing, _tree, _editor;
    protected Interface _iface;
    protected ImageLayer _image;
    protected EditableModelResource _model = new EditableModelResource();
    protected Selector _selector;
    protected final MouseInput _minput = new MouseInput();
    protected final Iterable<String> _images;

    protected static final int LISTING_WIDTH = 200, LISTING_HEIGHT = 400;
    protected static final int TREE_HEIGHT = SCREEN_SIZE.y() - LISTING_HEIGHT;
    protected static final int STAGE_WIDTH = SCREEN_SIZE.x() - LISTING_WIDTH * 2;
}
