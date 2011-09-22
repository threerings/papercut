//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import pythagoras.f.FloatMath;
import pythagoras.f.IDimension;

import react.Signal;
import react.Slot;

import playn.core.CanvasLayer;
import playn.core.PlayN;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Elements;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;

import flashbang.anim.rsrc.EditableLayerAnimation;

public class KeyframeDisplay extends Elements<KeyframeDisplay>
{
    public KeyframeDisplay (EditableLayerAnimation anim,
        final Signal<Integer> frameSelected) {
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
                    frameSelected.emit(frameIdx);
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

    protected final EditableLayerAnimation _anim;
}
