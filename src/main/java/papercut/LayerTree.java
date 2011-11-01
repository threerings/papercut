//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.CanvasLayer;
import playn.core.PlayN;

import pythagoras.f.Dimension;
import pythagoras.f.FloatMath;
import pythagoras.f.IDimension;

import react.Signal;
import react.UnitSignal;
import react.UnitSlot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Elements;
import tripleplay.ui.Field;
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
        super(new TableLayout(COL.fixed().alignLeft(), COL.stretch()).gaps(0, 5));

        _movie = movie;

        movie.treeChanged.connect(new UnitSlot () {
            @Override public void onEmit () {
                rebuild();
            }
        });
        rebuild();
    }

    public int frame () { return selected() == null ? 0 : selected().frame; }

    public void setFrame (int frame) {
        _selector.selected.update(_selector.selected.get().parent().childAt(frame));

    }

    /**
     * Returns currently selected layer if it's a group layer, the parent of the selected layer if
     * it isn't, or the root layer if there isn't a selected layer.
     */
    public EditableMovieGroupLayerConf groupLayer () {
        if (layer() != null) {
            if (layer() instanceof EditableMovieGroupLayerConf) {
                return (EditableMovieGroupLayerConf)layer();
            }
            return _movie.findParent(layer());
        }
        return _movie.root;
    }

    public EditableMovieLayerConf layer () { return selected() == null ? null : selected().layer; }

    public EditableAnimConf anim () { return selected().anim(); }

    protected Cell selected() { return (Cell)_selector.selected.get(); }

    protected void rebuild () {
        destroyAll();
        _selector = new Selector();
        _selector.selected.connect(new UnitSlot () {
            @Override public void onEmit () {
                frameSelected.emit();
            }
        });
        for (EditableMovieLayerConf child : _movie.root.children) {
            add(child, 0);
        }
    }

    protected void add (final EditableMovieLayerConf movieLayer, int indent) {
        Group keyframes = new Group(AxisLayout.horizontal().gap(0)) {
            @Override protected IDimension preferredSize (float hintX, float hintY) {
                return new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
            }

            @Override protected LayoutData computeLayout (float hintX, float hintY) {
                LayoutData ld = super.computeLayout(hintX, hintY);
                while (childCount() * FRAME_WIDTH > hintX) {
                    destroyAt(childCount() - 1);
                }
                while ((childCount() + 1) * FRAME_WIDTH < hintX) {
                    add(new Cell(movieLayer, childCount()));
                }
                if (childCount() > 0 && selected() == null) {
                    _selector.selected.update(childAt(0));
                }
                return ld;
            }
        };
        _selector.add(keyframes);
        Background indenter = Background.solid(0xFFFFFFFF, 0, 0, 0, 10 * indent);
        if (movieLayer instanceof EditableMovieGroupLayerConf) {
            Field field = new Field(movieLayer.name(), make(BACKGROUND.is(indenter)));
            field.text.connect(movieLayer.name.slot());
            add(field, keyframes);
            for (EditableMovieLayerConf child : ((EditableMovieGroupLayerConf)movieLayer).children) {
                add(child, indent + 1);
            }
        } else {
            add(new Label(movieLayer.name(), make(BACKGROUND.is(indenter))), keyframes);
        }
    }

    protected Selector _selector;

    protected final EditableMovieConf _movie;

    protected class Cell extends Button {
        public final int frame;
        public final EditableMovieLayerConf layer;

        public Cell (EditableMovieLayerConf layer, int frame) {
            this.layer = layer;
            this.frame = frame;
            Background defaultBackground = new LinedBackground();
            if (anim().hasKeyframe(frame)) {
                defaultBackground = new LinedBackground(0xFF00FF00);
            }
            setStyles(make(BACKGROUND.is(defaultBackground)).
                addSelected(BACKGROUND.is(new LinedBackground(0xFFFF0000))));
        }

        public EditableAnimConf anim () { return layer.animation(_movie.animation.get()); }
    }

    protected static class LinedBackground extends Background {
        public LinedBackground () {
            this(0xFFFFFFFF);
        }

        public LinedBackground (int fill) {
            super(FRAME_HEIGHT, FRAME_WIDTH, 0, 0);
            _fill = fill;
        }

        @Override protected Instance instantiate (IDimension size) {
            int cwidth = FloatMath.iceil(size.width()), cheight = FloatMath.iceil(size.height());
            CanvasLayer canvas = PlayN.graphics().createCanvasLayer(cwidth, cheight);
            canvas.canvas().setFillColor(_fill);
            canvas.canvas().fillRect(0, 0, size.width(), size.height());
            canvas.canvas().setStrokeColor(0xFF000000);
            canvas.canvas().strokeRect(0, 0, size.width() - 1, size.height() - 1);
            return new LayerInstance(canvas);
        }

        protected final int _fill;
    }

    protected static final int FRAME_HEIGHT = 14;
    protected static final int FRAME_WIDTH = 8;
}
