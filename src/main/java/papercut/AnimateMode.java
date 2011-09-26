//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import pythagoras.f.Rectangle;
import pythagoras.f.IPoint;
import pythagoras.f.Rectangle;

import react.Slot;
import react.UnitSlot;

import playn.core.Font;
import playn.core.ImageLayer;
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

import static tripleplay.ui.Style.*;
import static tripleplay.ui.Styles.make;

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

        _listing = _iface.createRoot(AxisLayout.vertical().gap(0), ROOT, modeLayer).
            setStyles(make(HALIGN.left, VALIGN.top)).setSize(LISTING_WIDTH, LISTING_HEIGHT);
        for (String image : _images) {
            _listing.add(new Button().setText(image));
        }
        _selector = new Selector().add(_listing).setSelected(_listing.childAt(0));

        _editor = _iface.createRoot(AxisLayout.horizontal(), ROOT, modeLayer).
            setBounds(LISTING_WIDTH + STAGE_WIDTH, 0, LISTING_WIDTH, LISTING_HEIGHT);
        final KeyframeEditor editor = new KeyframeEditor();
        _editor.add(editor);

        _tree = _iface.createRoot(AxisLayout.vertical().gap(0), ROOT, modeLayer).
            setStyles(make(VALIGN.top)).setBounds(0, LISTING_HEIGHT, SCREEN_SIZE.x(), TREE_HEIGHT);
        final LayerTree lt = new LayerTree(_model.animations.get("default"));
        _tree.add(lt);
        lt.frameSelected.connect(new UnitSlot () {
            @Override public void onEmit () {
                editor.setFrame(lt.layer(), lt.frame());
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
                if (_displayed != null) {
                    _displayed.destroySelf();
                }
                ImageLayerDesc desc = new ImageLayerDesc();
                desc.imageName = desc.name = imageName();
                _model.layers.add(desc);

                EditableLayerAnimation layerAnim = new EditableLayerAnimation(desc.imageName);
                layerAnim.keyframes.get(KeyframeType.X_LOCATION).value.update(
                    ev.x() - _listing.size().width());
                layerAnim.keyframes.get(KeyframeType.Y_LOCATION).value.update(ev.y());
                _model.animations.get("default").layers.add(layerAnim);

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
        return ((Button)_selector.selected().get()).text();
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
