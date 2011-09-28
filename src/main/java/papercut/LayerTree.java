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
import tripleplay.ui.Stylesheet;
import tripleplay.ui.TableLayout;

import flashbang.anim.rsrc.EditableAnimConf;
import flashbang.anim.rsrc.EditableMovieLayerConf;
import flashbang.anim.rsrc.EditableMovieConf;

import static tripleplay.ui.Style.*;
import static tripleplay.ui.Styles.make;
import static tripleplay.ui.TableLayout.COL;

public class LayerTree extends Elements<LayerTree>
{
    public final UnitSignal frameSelected = new UnitSignal();

    public LayerTree (EditableMovieConf model) {
        super(new TableLayout(COL.alignRight().fixed(), COL.fixed()).gaps(0, 5));
        setStylesheet(CELL);

        _model = model;

        for (EditableMovieLayerConf layer : model.children) {
            _layerAddListener.onAdd(layer);
        }
        model.children.connect(_layerAddListener);
        _selector.selectedChanged().connect(new UnitSlot () {
            @Override public void onEmit () {
                frameSelected.emit();
            }
        });
    }

    public int frame () { return selected() == null ? 0 : selected().frame; }

    public EditableAnimConf anim () {
        return selected().layer.animation(_model.animation.get());
    }

    protected Cell selected() { return (Cell)_selector.selected(); }

    protected final RList.Listener<EditableMovieLayerConf> _layerAddListener =
        new RList.Listener<EditableMovieLayerConf>() {
        @Override public void onAdd (final EditableMovieLayerConf modelLayer) {
            // TODO - layer nesting
            Group keyframes = new Group(AxisLayout.horizontal().gap(0)) {
                @Override protected LayoutData computeLayout (float hintX, float hintY) {
                    LayoutData ld = super.computeLayout(hintX, hintY);
                    while (childCount() * FRAME_WIDTH > hintX) {
                        removeAt(childCount() - 1);
                    }
                    while ((childCount() + 1) * FRAME_WIDTH < hintX) {
                        add(new Cell(modelLayer, childCount()));
                    }
                    if (childCount() > 0 && selected() == null) {
                        _selector.setSelected(childAt(0));
                    }
                    return ld;
                }
            };
            _selector.add(keyframes);
            add(new Label(modelLayer.name()), keyframes);
        }
    };

    protected final EditableMovieConf _model;
    protected final Selector _selector = new Selector();

    protected static class Cell extends Button {
        public final int frame;
        public final EditableMovieLayerConf layer;

        public Cell (EditableMovieLayerConf layer, int frame) {
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
