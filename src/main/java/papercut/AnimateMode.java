//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import react.Slot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Button;
import tripleplay.ui.Interface;
import tripleplay.ui.Root;

import com.threerings.flashbang.AppMode;

public class AnimateMode extends AppMode
{
    public AnimateMode (Iterable<String> assets) {
        _assets = assets;
    }

    @Override protected void setup () {
        super.setup();

        _iface = new Interface(input.plistener);
        _root = _iface.createRoot(AxisLayout.vertical().alignLeft());
        _modeLayer.add(_root.layer);
        for (String asset : _assets) {
            if (!asset.endsWith(".png")) { continue; }
            Button adder = new Button().setText(asset);
            _root.add(adder);
            adder.click.connect(new Slot<Button> () {
                @Override public void onEmit (Button b) {
                    System.out.println("Clicked: " + b.text());
                }
            });
        }
        _root.pack();
        _iface.activate();
    }


    @Override public void update (float dt) {
        super.update(dt);
        _iface.paint(0);
    }

    protected Root _root;
    protected Interface _iface;
    protected final Iterable<String> _assets;
}
