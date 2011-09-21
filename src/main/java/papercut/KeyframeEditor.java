//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import react.Slot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Elements;
import tripleplay.ui.Slider;

import flashbang.anim.rsrc.KeyframeDesc;

public class KeyframeEditor extends Elements<KeyframeEditor>
{
    public KeyframeEditor () {
        super(AxisLayout.vertical());
        add(_rotation);
        _rotation.value.connect(new Slot<Float> () {
            @Override public void onEmit (Float val) {
                _desc.rotation = val;
            }
        });
    }

    public void setKeyframe (KeyframeDesc desc) {
        _desc = desc;
        _rotation.value.update(desc.rotation);
    }

    protected final Slider _rotation = new Slider(0, -180, 180);
    protected KeyframeDesc _desc;
}
