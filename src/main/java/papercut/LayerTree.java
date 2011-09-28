//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import pythagoras.f.FloatMath;
import pythagoras.f.IDimension;

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
import tripleplay.ui.Styles;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.TableLayout;

import flashbang.anim.rsrc.EditableAnimConf;
import flashbang.anim.rsrc.EditableMovieConf;
import flashbang.anim.rsrc.EditableMovieGroupLayerConf;
import flashbang.anim.rsrc.EditableMovieLayerConf;

import static tripleplay.ui.Style.*;
import static tripleplay.ui.Styles.make;
import static tripleplay.ui.TableLayout.COL;

public class LayerTree extends Elements<LayerTree>
{
    public final UnitSignal frameSelected = new UnitSignal();

    public LayerTree (EditableMovieConf movie) {
        super(new TableLayout(COL.alignRight().fixed(), COL.fixed()).gaps(0, 5));
        setStylesheet(CELL);

        _movie = movie;

        movie.treeChanged.connect(new UnitSlot () {
            @Override public void onEmit () {
                rebuild();
            }
        });
        _selector.selectedChanged().connect(new UnitSlot () {
            @Override public void onEmit () {
                frameSelected.emit();
            }
        });
        rebuild();
    }

    public int frame () { return selected() == null ? 0 : selected().frame; }

    /**
     * Returns currently selected layer if it's a group layer, the parent of the selected layer if
     * it isn't, or the root layer if there isn't a selected layer.
     */
    public EditableMovieGroupLayerConf groupLayer () {
        if(layer() != null) {
            if (layer() instanceof EditableMovieGroupLayerConf) {
                return (EditableMovieGroupLayerConf)layer();
            }
            return _movie.findParent(layer());
        }
        return _movie.root;
    }

    public EditableMovieLayerConf layer () { return selected() == null ? null : selected().layer; }

    public EditableAnimConf anim () { return layer().animation(_movie.animation.get()); }

    protected Cell selected() { return (Cell)_selector.selected(); }

    protected void rebuild () {
        removeAll();
        for (EditableMovieLayerConf child : _movie.root.children) {
            add(child, 0);
        }
    }

    protected void add (final EditableMovieLayerConf movieLayer, int indent) {
        Group keyframes = new Group(AxisLayout.horizontal().gap(0)) {
            @Override protected LayoutData computeLayout (float hintX, float hintY) {
                LayoutData ld = super.computeLayout(hintX, hintY);
                while (childCount() * FRAME_WIDTH > hintX) {
                    removeAt(childCount() - 1);
                }
                while ((childCount() + 1) * FRAME_WIDTH < hintX) {
                    add(new Cell(movieLayer, childCount()));
                }
                if (childCount() > 0 && selected() == null) {
                    _selector.setSelected(childAt(0));
                }
                return ld;
            }
        };
        _selector.add(keyframes);
        Background indenter = Background.solid(0xFFFFFFFF, 0, 5 * indent, 0, 0);
        add(new Label(movieLayer.name(), make(BACKGROUND.is(indenter))), keyframes);
        if (movieLayer instanceof EditableMovieGroupLayerConf) {
            for (EditableMovieLayerConf child : ((EditableMovieGroupLayerConf)movieLayer).children) {
                add(child, indent + 1);
            }
        }
    }

    protected final EditableMovieConf _movie;
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
