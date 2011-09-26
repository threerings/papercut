//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import pythagoras.f.FloatMath;
import pythagoras.f.IDimension;

import react.RList;
import react.Signal;
import react.UnitSignal;
import react.UnitSlot;

import playn.core.CanvasLayer;
import playn.core.PlayN;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Elements;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Selector;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.TableLayout;

import flashbang.anim.rsrc.EditableLayerAnimation;
import flashbang.anim.rsrc.EditableModelAnimation;
import flashbang.anim.rsrc.ModelAnimation;

import static tripleplay.ui.Style.*;
import static tripleplay.ui.Styles.make;

public class LayerTree extends Elements<LayerTree>
{
    public final UnitSignal frameSelected = new UnitSignal();

    public LayerTree (EditableModelAnimation animation) {
        super(createLayout());
        setStylesheet(CELL);

        _anim = animation;

        for (EditableLayerAnimation layer : _anim.layers()) {
            _layerAddListener.onAdd(layer);
        }
        animation.layers.connect(_layerAddListener);
        _selector.selected().connect(new UnitSlot () {
            @Override public void onEmit () {
                frameSelected.emit();
            }
        });
    }

    protected static TableLayout createLayout () {
        TableLayout layout = new TableLayout(2).gaps(0, 5);
        layout.column(0).alignRight().fixed();
        layout.column(1).fixed();
        return layout;
    }

    public int frame () { return ((Cell)_selector.selected().get()).frame; }

    public EditableLayerAnimation layer () { return ((Cell)_selector.selected().get()).layer; }

    protected final RList.Listener<EditableLayerAnimation> _layerAddListener =
        new RList.Listener<EditableLayerAnimation>() {
        @Override public void onAdd (final EditableLayerAnimation anim) {
            // TODO - layer nesting
            Group keyframes = new Group(AxisLayout.horizontal().gap(0)) {
                @Override protected LayoutData computeLayout (float hintX, float hintY) {
                    LayoutData ld = super.computeLayout(hintX, hintY);
                    while (childCount() * FRAME_WIDTH > hintX) {
                        removeAt(childCount() - 1);
                    }
                    while ((childCount() + 1) * FRAME_WIDTH < hintX) {
                        add(new Cell(anim, childCount() - 1));
                    }
                    if (childCount() > 0 && _selector.selected().get() == null) {
                        _selector.setSelected(childAt(0));
                    }
                    return ld;
                }
            };
            _selector.add(keyframes);
            add(new Label(anim.layerSelector()), keyframes);
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
            super(FRAME_HEIGHT, FRAME_WIDTH, 0, 0);
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

    protected static final int FRAME_HEIGHT = 14;
    protected static final int FRAME_WIDTH = 8;

    protected static final Stylesheet CELL = Stylesheet.builder().add(Cell.class,
        make(BACKGROUND.is(new LinedBackground())).
        addSelected(BACKGROUND.is(Background.solid(0xFFFF0000, FRAME_HEIGHT, FRAME_WIDTH, 0, 0)))).
        create();


}
