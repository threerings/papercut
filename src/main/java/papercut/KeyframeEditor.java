//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import react.Function;
import react.Slot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Elements;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Slider;

import flashbang.anim.rsrc.KeyframeDesc;

public class KeyframeEditor extends Elements<KeyframeEditor>
{
    public KeyframeEditor () {
        super(AxisLayout.vertical());
        addSlider(_rotation, "Rotation");
        _rotation.value.connect(new Slot<Float> () {
            @Override public void onEmit (Float val) {
                _desc.rotation = val;
            }
        });
    }

    protected void addSlider(Slider slider, String name) {
        slider.setConstraint(AxisLayout.stretched());
        Group row = new Group(AxisLayout.horizontal());
        Label value;
        add(new Group(AxisLayout.horizontal()).add(new Label(name), slider, value = new Label()));
        slider.value.map(FORMAT).connect(value.textSlot());
        slider.value.updateForce(slider.value.get());
    }

    public void setKeyframe (KeyframeDesc desc) {
        _desc = desc;
        _rotation.value.update(desc.rotation);
    }

    protected KeyframeDesc _desc;

    protected final Slider _rotation = new Slider(0, -180, 180);

    protected static Function<Float, String> FORMAT = new Function<Float, String>() {
        @Override public String apply (Float v) {
            return _format.format(v);
        }

        // TODO - should this use GWT's NumberFormat?
        //protected final NumberFormat _format = NumberFormat.getFormat("0.00");
        protected final NumberFormat _format = new DecimalFormat("000.00");
    };
}
