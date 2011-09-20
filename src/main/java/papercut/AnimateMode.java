//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.Mouse;
import playn.core.PlayN;

import pythagoras.f.Dimension;
import pythagoras.f.Rectangle;

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
import flashbang.anim.rsrc.ModelResource;
import flashbang.rsrc.ImageResource;

public class AnimateMode extends AppMode
{
    public AnimateMode (Iterable<String> images) {
        _images = images;
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

        _tree = _iface.createRoot(AxisLayout.vertical().alignLeft().gap(0));
        modeLayer.add(_tree.layer);
        _tree.layer.setTranslation(0, LISTING_HEIGHT);
        _tree.add(new LayerTree(_model));
        _tree.setSize(LISTING_WIDTH, PapercutApp.SCREEN_SIZE.y() - LISTING_HEIGHT);


        PlayN.mouse().setListener(minput.mlistener);
        Rectangle listingBounds = new Rectangle(new Dimension(LISTING_WIDTH,
            PapercutApp.SCREEN_SIZE.y()));
        minput.register(new Input.BoundsRegion(listingBounds), new Mouse.Adapter() {
            @Override public void onMouseMove (Mouse.MotionEvent ev) {
                if (_image != null) {
                    _image.destroy();
                    _image = null;
                }
            }
        });

        Rectangle stageBounds = new Rectangle(LISTING_WIDTH, 0,
            PapercutApp.SCREEN_SIZE.x() - LISTING_WIDTH, PapercutApp.SCREEN_SIZE.y());
        minput.register(new Input.BoundsRegion(stageBounds), new Mouse.Adapter() {
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
    }

    protected String imageName () {
        return ((Button)_selector.selected()).text();
    }

    @Override public void update (float dt) {
        super.update(dt);
        _iface.paint(0);
    }

    protected Root _listing, _tree;
    protected Interface _iface;
    protected ImageLayer _image;
    protected ModelResource _model = new ModelResource("test");
    protected Layer _modelLayer;
    protected Selector _selector;
    protected final MouseInput minput = new MouseInput();
    protected final Iterable<String> _images;
    protected static final int LISTING_WIDTH = 200, LISTING_HEIGHT = 400;
}
