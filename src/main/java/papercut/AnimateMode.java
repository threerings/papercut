//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

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

public class AnimateMode extends AppMode
{
    public AnimateMode (Iterable<String> assets) {
        _assets = assets;
    }

    @Override protected void setup () {
        super.setup();

        final Styles selectedStyles = Styles.none().
            add(Style.COLOR.is(0xFFFFFFFF)).
            add(Style.BACKGROUND.is(Background.solid(0xFF000000)));

        final Styles deselectedStyles = Styles.none().
            add(Style.COLOR.is(0xFF000000)).
            add(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF)));

        _iface = new Interface(pointerListener());
        _root = _iface.createRoot(AxisLayout.vertical().alignLeft());
        modeLayer.add(_root.layer);
        for (String asset : _assets) {
            if (!asset.endsWith(".png")) { continue; }
            Button adder = new Button().setText(asset);
            _root.add(adder);
            adder.click.connect(new Slot<Button> () {
                @Override public void onEmit (Button b) {
                    if (_selected != null) {
                        _selected.addStyles(deselectedStyles);
                    }
                    _selected = b;
                    _selected.addStyles(selectedStyles);
                }
            });
        }
        _root.pack();
        PlayN.pointer().setListener(_iface.plistener);
    }


    @Override public void update (float dt) {
        super.update(dt);
        _iface.paint(0);
    }

    protected Root _root;
    protected Interface _iface;
    protected Button _selected;
    protected final Iterable<String> _assets;
}
