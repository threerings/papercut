//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import tripleplay.ui.Style;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import pythagoras.f.MathUtil;

import react.Slot;
import react.UnitSignal;
import react.UnitSlot;

import tripleplay.ui.Button;
import tripleplay.ui.Constraints;
import tripleplay.ui.Element;
import tripleplay.ui.Elements;
import tripleplay.ui.Field;
import tripleplay.ui.Interface;
import tripleplay.ui.Label;
import tripleplay.ui.Slider;
import tripleplay.ui.TableLayout;

import flashbang.anim.rsrc.EditableAnimConf;
import flashbang.anim.rsrc.EditableKeyframeConf;
import flashbang.anim.rsrc.InterpolatorType;
import flashbang.anim.rsrc.KeyframeType;

import static tripleplay.ui.TableLayout.COL;

public class KeyframeEditor extends Elements<KeyframeEditor>
{
    public final UnitSignal edited = new UnitSignal();

    public KeyframeEditor (final Interface iface) {
        super(new TableLayout(COL.fixed().alignRight(), COL, COL.fixed().alignRight(),
                              COL.fixed().minWidth(80).alignLeft()).gaps(2, 2));
        for (final KeyframeType kt : KeyframeType.values()) {
            final Slider slider = createSlider(kt);
            _sliders.put(kt, slider);

            final Field entry = new Field().setStyles(Style.HALIGN.right).
                setConstraint(Constraints.fixedSize("-000.00"));
            UnitSlot entryFromSlider = new UnitSlot () {
                @Override public void onEmit () {
                    if (entry.isFocused()) return;// Don't reformat if the user is changing the text
                    float value = slider.value.get();
                    entry.text.update(format(value, 2));
                }
            };
            entryFromSlider.onEmit();// Fill in the field with the current slider value

            final List<String> interpNames = Lists.newArrayList();
            for (InterpolatorType type : InterpolatorType.values()) {
                interpNames.add(type.name());
            }
            final Button interp = new Button();
            interp.clicked().connect(new UnitSlot() {
                @Override public void onEmit () {
                    AnimateMode.popup(iface, interp.layer, interpNames, interp.text.slot());
                }
            });
            _interps.put(kt, interp);

            add(new Label(kt.displayName), slider, entry, interp);

            slider.value.connect(entryFromSlider);// Update the text on slider changes
            entry.defocused.connect(entryFromSlider);// Reformat after finishing with text

            // Update the slider with valid field values while editing
            entry.text.connect(new Slot<String> () {
                @Override public void onEmit (String value) {
                    if (!entry.isFocused()) return;
                    try {
                        slider.value.update(Float.parseFloat(value));
                    } catch (NumberFormatException nfe) {}
                }
            });

            UnitSlot updateFrame = new UnitSlot () {
                @Override public void onEmit () {
                    if (_anim == null || _updating) return;
                    String t = interp.text.get();
                    _anim.add(kt, _frame, slider.value.get(),
                        InterpolatorType.valueOf(t));
                    edited.emit();
                }
            };
            // Update the animation whenever the slider changes
            slider.value.connect(updateFrame);
            interp.text.connect(updateFrame);
        }
    }

    protected static String format (float value, int decimals) {
        StringBuilder buf = new StringBuilder();
        if (value < 0) {
            buf.append("-");
            value = -value;
        }
        int ivalue = (int)value;
        buf.append(ivalue);
        buf.append(".");
        for (int ii = 0; ii < decimals; ii++) {
            value = (value - ivalue) * 10;
            ivalue = (int)value;
            buf.append(ivalue);
        }
        return buf.toString();
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
        for (KeyframeType kt : KeyframeType.values()) {
            EditableKeyframeConf kf = _anim.keyframes.get(kt).find(_frame);
            _sliders.get(kt).value.update(kf.interp(_frame));
            _interps.get(kt).text.update(kf.interpolator.get().name());
        }
        _updating = false;
    }

    protected int _frame;
    protected EditableAnimConf _anim;
    protected boolean _updating;

    protected final Map<KeyframeType, Button> _interps =
        new EnumMap<KeyframeType, Button>(KeyframeType.class);
    protected final Map<KeyframeType, Slider> _sliders =
        new EnumMap<KeyframeType, Slider>(KeyframeType.class);
}
