//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import java.util.EnumMap;
import java.util.Map;

import pythagoras.f.MathUtil;

import react.Slot;
import react.UnitSignal;

import tripleplay.ui.Elements;
import tripleplay.ui.Label;
import tripleplay.ui.Slider;
import static tripleplay.ui.TableLayout.COL;
import tripleplay.ui.TableLayout;

import flashbang.anim.rsrc.EditableLayerAnimation;
import flashbang.anim.rsrc.KeyframeType;

public class KeyframeEditor extends Elements<KeyframeEditor>
{
    public final UnitSignal edited = new UnitSignal();

    public KeyframeEditor () {
        super(new TableLayout(COL.fixed().alignRight(), COL).gaps(0, 2));
        for (final KeyframeType kt : KeyframeType.values()) {
            Slider slider = createSlider(kt);
            _sliders.put(kt, slider);
            add(new Label(kt.displayName), slider);
            slider.valueChanged().connect(new Slot<Float> () {
                @Override public void onEmit (Float val) {
                    if (_layer == null || _updating) return;
                    _layer.add(kt, _frame, val);
                    edited.emit();
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

    public final Slot<Integer> frameSlot = new Slot<Integer> () {
        @Override public void onEmit (Integer frame) {
            _frame = frame;
            update();
        }
    };

    public final Slot<EditableLayerAnimation> layerSlot = new Slot<EditableLayerAnimation> () {
        @Override public void onEmit (EditableLayerAnimation layer) {
            _layer = layer;
            update();
        }
    };

    protected void update () {
        _updating = true;
        for (Map.Entry<KeyframeType, Slider> entry : _sliders.entrySet()) {
            float value = _layer.keyframes.get(entry.getKey()).find(_frame).interp(_frame);
            entry.getValue().setValue(value);
        }
        _updating = false;
    }

    protected int _frame;
    protected EditableLayerAnimation _layer;
    protected boolean _updating;

    protected final Map<KeyframeType, Slider> _sliders =
        new EnumMap<KeyframeType, Slider>(KeyframeType.class);
}
