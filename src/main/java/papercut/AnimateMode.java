//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.ImageLayer;
import playn.core.Mouse;
import playn.core.PlayN;

import react.Slot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Interface;
import tripleplay.ui.Root;
import tripleplay.ui.Selector;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;

import com.threerings.flashbang.AppMode;
import com.threerings.flashbang.rsrc.ImageResource;

public class AnimateMode extends AppMode
{
    public AnimateMode (Iterable<String> images) {
        _images = images;
    }

    @Override protected void setup () {
        super.setup();

        final Styles styles = Styles.
            make(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF, 2))).
            makeSelected(
                Style.COLOR.is(0xFFFFFFFF), Style.BACKGROUND.is(Background.solid(0xFF000000, 2)));

        _iface = new Interface(pointerListener());
        PlayN.pointer().setListener(_iface.plistener);

        _listing = _iface.createRoot(AxisLayout.vertical().alignLeft().gap(0));
        modeLayer.add(_listing.layer);
        new Selector(_listing).selected.connect(new Slot<Element<?>>() {
            @Override public void onEmit (Element<?> b) {
                if (_image != null) {
                    _image.destroy();
                }
                ImageResource rsrc = ImageResource.require(((Button)b).text());
                _image = PlayN.graphics().createImageLayer(rsrc.image());
                modeLayer.add(_image);
            }
        });
        for (String image : _images) {
            _listing.add(new Button(styles).setText(image));
        }
        _listing.packToWidth(200);

        PlayN.mouse().setListener(new Mouse.Adapter() {
            @Override public void onMouseMove (Mouse.MotionEvent ev) {
                if (_image == null) { return; }
                _image.setTranslation(ev.x(), ev.y());
            }
        });
    }

    @Override public void update (float dt) {
        super.update(dt);
        _iface.paint(0);
    }

    protected Root _listing, _stage;
    protected Interface _iface;
    protected ImageLayer _image;
    protected final Iterable<String> _images;
}
