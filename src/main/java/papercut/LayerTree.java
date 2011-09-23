//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.CanvasLayer;
import playn.core.PlayN;

import pythagoras.f.FloatMath;
import pythagoras.f.IDimension;

import react.RList;
import react.Signal;
import react.UnitSignal;
import react.UnitSlot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Elements;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Selector;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.Stylesheet;

import flashbang.anim.rsrc.EditableLayerAnimation;
import flashbang.anim.rsrc.EditableModelAnimation;
import flashbang.anim.rsrc.ModelAnimation;

public class LayerTree extends Elements<LayerTree>
{
    public final UnitSignal frameSelected = new UnitSignal();

    public LayerTree (EditableModelAnimation animation) {
        super(AxisLayout.vertical());
        _anim = animation;

        for (EditableLayerAnimation layer : _anim.layers()) {
            _layerAddListener.onAdd(layer);
        }
        animation.layers.listen(_layerAddListener);
        _selector.selected().connect(new UnitSlot () {
            @Override public void onEmit () {
                frameSelected.emit();
            }
        });
    }

    public int frame () { return ((Cell)_selector.selected().get()).frame; }

    public EditableLayerAnimation layer () { return ((Cell)_selector.selected().get()).layer; }

    protected final RList.Listener<EditableLayerAnimation> _layerAddListener =
        new RList.Listener<EditableLayerAnimation>() {
        @Override public void onAdd (EditableLayerAnimation anim) {
            // TODO - layer nesting
            Group keyframes = new Group(AxisLayout.horizontal().gap(0)).setStylesheet(CELL);
            for (int ii = 0; ii < 50; ii++) {
                keyframes.add(new Cell(anim, ii));
            }
            _selector.add(keyframes);
            add(new Group(AxisLayout.horizontal()).add(new Label(anim.layerSelector()), keyframes));
            if (childCount() == 1) {
                _selector.setSelected(keyframes.childAt(0));
            }
        }
    };

    protected final EditableModelAnimation _anim;
    protected final Selector _selector = new Selector();

    protected static class Cell extends Button {
        public final int frame;
        public final EditableLayerAnimation layer;

        public Cell (EditableLayerAnimation layer, int frame) {
            this.layer = layer;
            this.frame = frame;
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

    protected static final Stylesheet CELL = Stylesheet.builder().add(Cell.class,
        Styles.make(Style.BACKGROUND.is(new LinedBackground())).
            addSelected(Style.BACKGROUND.is(Background.solid(0xFFFF0000, 7, 4, 7, 4)))).create();
}
