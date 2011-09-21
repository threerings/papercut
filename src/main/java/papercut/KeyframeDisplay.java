//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.CanvasLayer;
import playn.core.PlayN;

import pythagoras.f.FloatMath;
import pythagoras.f.IDimension;

import react.Signal;
import react.Slot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Elements;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;

import flashbang.anim.rsrc.KeyframeDesc;
import flashbang.anim.rsrc.LayerAnimDesc;

public class KeyframeDisplay extends Elements<KeyframeDisplay>
{
    public KeyframeDisplay (LayerAnimDesc anim, final Signal<KeyframeDesc> frameSelected) {
        super(AxisLayout.horizontal().gap(0));
        _anim = anim;

        // TODO - check for values in existing keyframes
        Styles frameStyle = Styles.make(Style.BACKGROUND.is(new LinedBackground()));
        for (int ii = 0; ii < 500; ii += 8) {
            final int frameIdx = ii;
            Button frame = new Button().setStyles(frameStyle);
            add(frame);
            frame.clicked().connect(new Slot<Button> () {
                @Override public void onEmit (Button b) {
                    for (KeyframeDesc kf : _anim.keyframes) {
                        if (kf.frameIdx == frameIdx) {
                            frameSelected.emit(kf);
                            return;
                        }
                    }
                    _anim.keyframes.add(new KeyframeDesc(frameIdx));
                    frameSelected.emit(_anim.keyframes.get(_anim.keyframes.size() - 1));
                }
            });
        }
    }

    protected static class LinedBackground extends Background {
        public LinedBackground () {
            super(7, 4, 7, 4);
        }

        @Override protected Instance instantiate (IDimension size) {
            int cwidth = FloatMath.iceil(size.width()), cheight = FloatMath.iceil(size.height());
            CanvasLayer canvas = PlayN.graphics().createCanvasLayer(cwidth, cheight);
            canvas.canvas().setFillColor(0xFFFFFFFF);
            canvas.canvas().fillRect(0, 0, size.width(), size.height());
            canvas.canvas().setStrokeColor(0xFF000000);
            canvas.canvas().strokeRect(0, 0, size.width() - 1, size.height() - 1);
            return new LayerInstance(canvas);
        }
    }

    protected final LayerAnimDesc _anim;
}
