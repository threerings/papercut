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
import tripleplay.ui.Interface;
import tripleplay.ui.Root;
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

        final Styles selectedStyles = Styles.none().
            add(Style.COLOR.is(0xFFFFFFFF), Style.BACKGROUND.is(Background.solid(0xFF000000, 2)));


        final Styles deselectedStyles = Styles.none().
            add(Style.COLOR.is(0xFF000000), Style.BACKGROUND.is(Background.solid(0xFFFFFFFF, 2)));

        _iface = new Interface(pointerListener());
        PlayN.pointer().setListener(_iface.plistener);

        _listing = _iface.createRoot(AxisLayout.vertical().alignLeft().gap(0));
        modeLayer.add(_listing.layer);
        for (final String image : _images) {
            Button adder = new Button(deselectedStyles).setText(image);
            _listing.add(adder);
            adder.click.connect(new Slot<Button> () {
                @Override public void onEmit (Button b) {
                    if (_selected != null) {
                        _selected.addStyles(deselectedStyles);
                        _image.destroy();
                    }
                    _selected = b;
                    ImageResource rsrc = ImageResource.require(image);
                    _image = PlayN.graphics().createImageLayer(rsrc.image());
                    modeLayer.add(_image);

                    _selected.addStyles(selectedStyles);
                }
            });
        }
        _listing.packToWidth(200);

        PlayN.mouse().setListener(new Mouse.Adapter() {
            @Override public void onMouseMove (Mouse.MotionEvent ev) {
                if (_selected == null) { return; }
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
    protected Button _selected;
    protected ImageLayer _image;
    protected final Iterable<String> _images;
}
