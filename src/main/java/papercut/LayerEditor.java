//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import react.UnitSlot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Button;
import tripleplay.ui.Elements;

import flashbang.anim.rsrc.EditableMovieConf;
import flashbang.anim.rsrc.EditableMovieGroupLayerConf;
import flashbang.anim.rsrc.EditableMovieLayerConf;
import flashbang.anim.rsrc.MovieConf;

public class LayerEditor extends Elements<LayerEditor>
{
    public LayerEditor (final EditableMovieConf conf, final LayerTree tree) {
        super(AxisLayout.horizontal());
        add(_add, _remove, _indent, _dedent);
        _add.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                conf.add(tree.groupLayer(), new EditableMovieGroupLayerConf("Group"));
            }
        });
        _remove.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                EditableMovieLayerConf toRemove = tree.layer();
                if (toRemove == null) return;
                conf.findParent(toRemove).children.remove(toRemove);
            }
        });
        _indent.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                EditableMovieLayerConf toIndent = tree.layer();
                if (toIndent == null) return;
                EditableMovieGroupLayerConf currentParent = conf.findParent(toIndent);
                int idx = currentParent.children.indexOf(toIndent);
                currentParent.children.remove(idx);
                if (idx != 0) {
                     EditableMovieLayerConf prevSib =  currentParent.children.get(idx - 1);
                     if (prevSib instanceof EditableMovieGroupLayerConf) {
                         ((EditableMovieGroupLayerConf)prevSib).children.add(toIndent);
                         return;
                     }
                }
                EditableMovieGroupLayerConf newParent = new EditableMovieGroupLayerConf("Group");
                conf.add(currentParent, newParent, idx);
                newParent.children.add(toIndent);
            }
        });
        _dedent.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                EditableMovieLayerConf toDedent = tree.layer();
                if (toDedent == null) return;
                EditableMovieGroupLayerConf currentParent = conf.findParent(toDedent);
                if (currentParent == conf.root) return;
                currentParent.children.remove(toDedent);
                EditableMovieGroupLayerConf newParent = conf.findParent(currentParent);
                newParent.children.add(newParent.children.indexOf(currentParent) + 1, toDedent);
            }
        });
    }

    protected final Button _add = new Button("+"), _remove = new Button("-"),
        _indent = new Button(">"), _dedent = new Button("<");
}
