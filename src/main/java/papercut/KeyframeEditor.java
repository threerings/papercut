//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import java.util.EnumMap;
import java.util.Map;

import pythagoras.f.MathUtil;

import react.Slot;

import tripleplay.ui.Elements;
import tripleplay.ui.Label;
import tripleplay.ui.Slider;
import tripleplay.ui.TableLayout.Column;
import tripleplay.ui.TableLayout;

import flashbang.anim.rsrc.EditableLayerAnimation;
import flashbang.anim.rsrc.KeyframeType;

import static flashbang.anim.rsrc.KeyframeType.ROTATION;

public class KeyframeEditor extends Elements<KeyframeEditor>
{
    public KeyframeEditor () {
        super(new TableLayout(new Column().fixed().alignRight(), new Column()).gaps(0, 2));
        for (final KeyframeType kt : KeyframeType.values()) {
            Slider slider = createSlider(kt);
            add(new Label(kt.displayName), slider);
            slider.value.connect(new Slot<Float> () {
                @Override public void onEmit (Float val) {
                    if (_layer == null || _updatingFrame) return;
                    _layer.add(kt, _frame, val);
                }
            });
        }
    }

    protected static Slider createSlider (KeyframeType kt) {
        switch (kt) {
            case X_LOCATION:
            case Y_LOCATION:
                return new Slider(0, -500, 500);
            case X_SCALE:
            case Y_SCALE:
                return new Slider(1, -10, 10);
            case ROTATION:
                return new Slider(0, 0, MathUtil.TWO_PI);// TAU CAN SUCK IT, GREENWELL
            case ALPHA:
                return new Slider(1, 0, 1);
            default:
                throw new RuntimeException("Unhandled keyframe type: " + kt);
        }
    }

    public void setFrame (EditableLayerAnimation layer, int frame) {
        _updatingFrame = true;
        _frame = frame;
        _layer = layer;
        for (Map.Entry<KeyframeType, Slider> entry : _sliders.entrySet()) {
            float value = _layer.keyframes.get(entry.getKey()).find(frame).interp(frame);
            entry.getValue().value.update(value);
        }
        _updatingFrame = false;
    }

    protected int _frame;
    protected EditableLayerAnimation _layer;
    protected boolean _updatingFrame;

    protected final Map<KeyframeType, Slider> _sliders =
        new EnumMap<KeyframeType, Slider>(KeyframeType.class);
}
