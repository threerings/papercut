//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import pythagoras.f.MathUtil;

import react.Function;
import react.Slot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Elements;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Slider;

import flashbang.anim.rsrc.EditableLayerAnimation;

import static flashbang.anim.rsrc.KeyframeType.ROTATION;

public class KeyframeEditor extends Elements<KeyframeEditor>
{
    public KeyframeEditor () {
        super(AxisLayout.vertical());
        addSlider(_rotation, "Rotation");
        _rotation.value.connect(new Slot<Float> () {
            @Override public void onEmit (Float val) {
                if (_updatingFrame) return;
                _layer.add(ROTATION, _frame, (float)Math.toRadians(val));
            }
        });
    }

    protected void addSlider(Slider slider, String name) {
        slider.setConstraint(AxisLayout.stretched());
        Group row = new Group(AxisLayout.horizontal());
        Label value;
        add(new Group(AxisLayout.horizontal()).add(new Label(name), slider, value = new Label()));
        slider.value.map(FORMAT).connectNotify(value.textSlot());
    }

    public void setFrame (EditableLayerAnimation layer, int frame) {
        _updatingFrame = true;
        _frame = frame;
        _layer = layer;
        _rotation.value.update(_layer.keyframes.get(ROTATION).find(frame).interp(frame));
        _updatingFrame = false;
    }

    protected int _frame;
    protected EditableLayerAnimation _layer;
    protected boolean _updatingFrame;

    protected final Slider _rotation = new Slider(0, -180, 180);

    protected static Function<Float, String> FORMAT = new Function<Float, String>() {
        @Override public String apply (Float v) {
            return MathUtil.toString(v);
        }
    };
}
