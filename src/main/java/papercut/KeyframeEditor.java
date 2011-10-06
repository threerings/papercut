//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import java.util.EnumMap;
import java.util.Map;

import pythagoras.f.MathUtil;

import react.Slot;
import react.UnitSignal;
import react.UnitSlot;

import tripleplay.ui.Elements;
import tripleplay.ui.Field;
import tripleplay.ui.Label;
import tripleplay.ui.Slider;
import tripleplay.ui.TableLayout;

import flashbang.anim.rsrc.EditableAnimConf;
import flashbang.anim.rsrc.KeyframeType;

import static tripleplay.ui.TableLayout.COL;

public class KeyframeEditor extends Elements<KeyframeEditor>
{
    public final UnitSignal edited = new UnitSignal();

    public KeyframeEditor () {
        super(new TableLayout(COL.fixed().alignRight(), COL, COL.fixed().alignLeft()).gaps(0, 2));
        for (final KeyframeType kt : KeyframeType.values()) {
            final Slider slider = createSlider(kt);
            _sliders.put(kt, slider);
            final Field entry = new Field();

            UnitSlot entryFromSlider = new UnitSlot () {
                @Override public void onEmit () {
                    if (entry.focused()) return;// Don't reformat if the user is changing the text
                    entry.text.update(MathUtil.toString(slider.value.get(), 1));
                }
            };
            entryFromSlider.onEmit();// Fill in the field with the current slider value
            slider.value.connect(entryFromSlider);// Update the text on slider changes
            entry.defocused.connect(entryFromSlider);// Reformat after finishing with text

            add(new Label(kt.displayName), slider, entry);

             // Update the slider with valid field values while editing
             entry.text.connect(new Slot<String> () {
                @Override public void onEmit (String value) {
                    if (!entry.focused()) return;
                    try {
                        slider.value.update(Float.parseFloat(value));
                    } catch (NumberFormatException nfe) {}
                }
            });

            // Update the animation whenever the slider changes
            slider.value.connect(new Slot<Float> () {
                @Override public void onEmit (Float val) {
                    if (_anim == null || _updating) return;
                    _anim.add(kt, _frame, val);
                    edited.emit();
                }
            });
        }
    }

    protected static Slider createSlider (KeyframeType kt) {
        switch (kt) {
            case X_LOCATION:
            case Y_LOCATION:
            case X_ORIGIN:
            case Y_ORIGIN:
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

    public void setFrame (int frame, EditableAnimConf anim) {
        _frame = frame;
        _anim = anim;
        _updating = true;
        for (Map.Entry<KeyframeType, Slider> entry : _sliders.entrySet()) {
            float value = _anim.keyframes.get(entry.getKey()).find(_frame).interp(_frame);
            entry.getValue().value.update(value);
        }
        _updating = false;
    }

    protected int _frame;
    protected EditableAnimConf _anim;
    protected boolean _updating;

    protected final Map<KeyframeType, Slider> _sliders =
        new EnumMap<KeyframeType, Slider>(KeyframeType.class);
}
