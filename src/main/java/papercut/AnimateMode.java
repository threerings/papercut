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
import tripleplay.util.Input;
import tripleplay.util.MouseInput;

import flashbang.AppMode;
import flashbang.anim.rsrc.ImageLayerDesc;
import flashbang.anim.rsrc.KeyframeDesc;
import flashbang.anim.rsrc.ModelAnimDesc;
import flashbang.anim.rsrc.ModelResource;
import flashbang.rsrc.ImageResource;

import static papercut.PapercutApp.SCREEN_SIZE;

public class AnimateMode extends AppMode
{
    public AnimateMode (Iterable<String> images) {
        _images = images;
        _model.anims.put("default", new ModelAnimDesc());
    }

    @Override protected void setup () {
        super.setup();

        final Styles styles = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF, 2))).
            addSelected(
                Style.COLOR.is(0xFFFFFFFF), Style.BACKGROUND.is(Background.solid(0xFF000000, 2)));

        _iface = new Interface(pointerListener());
        PlayN.pointer().setListener(_iface.plistener);

        _listing = _iface.createRoot(AxisLayout.vertical().alignLeft().gap(0));
        modeLayer.add(_listing.layer);
        for (String image : _images) {
            _listing.add(new Button(styles).setText(image));
        }
        _selector = new Selector(_listing).setSelected(_listing.childAt(0));
        _listing.setSize(LISTING_WIDTH, LISTING_HEIGHT);

        _editor = _iface.createRoot(AxisLayout.horizontal());
        modeLayer.add(_editor.layer);
        _editor.layer.setTranslation(LISTING_WIDTH + STAGE_WIDTH, 0);
        final KeyframeEditor editor = new KeyframeEditor();
        _editor.add(editor);
        _editor.setSize(LISTING_WIDTH, LISTING_HEIGHT);

        _tree = _iface.createRoot(AxisLayout.vertical().alignLeft().gap(0));
        modeLayer.add(_tree.layer);
        _tree.layer.setTranslation(0, LISTING_HEIGHT);
        LayerTree lt = new LayerTree(_model, _model.anims.get("default"));
        _tree.add(lt);
        lt.frameSelected.connect(new Slot<KeyframeDesc> () {
            @Override public void onEmit (KeyframeDesc kf) {
                editor.setKeyframe(kf);
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
                if (_modelLayer != null) {
                    modeLayer.remove(_modelLayer);
                }
                ImageLayerDesc desc = new ImageLayerDesc();
                desc.imageName = desc.name = imageName();
                desc.x = ev.x() - _listing.size().width();
                desc.y = ev.y();
                _model.layers.add(desc);
                _modelLayer = _model.build();
                _modelLayer.setTranslation(_listing.size().width(), 0);
                modeLayer.add(_modelLayer);
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
    protected ModelResource _model = new ModelResource("test");
    protected Layer _modelLayer;
    protected Selector _selector;
    protected final MouseInput _minput = new MouseInput();
    protected final Iterable<String> _images;

    protected static final int LISTING_WIDTH = 200, LISTING_HEIGHT = 400;
    protected static final int TREE_HEIGHT = SCREEN_SIZE.y() - LISTING_HEIGHT;
    protected static final int STAGE_WIDTH = SCREEN_SIZE.x() - LISTING_WIDTH * 2;
}
