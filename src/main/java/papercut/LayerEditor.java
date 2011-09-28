//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import react.UnitSlot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Button;
import tripleplay.ui.Elements;

import flashbang.anim.rsrc.EditableMovieConf;
import flashbang.anim.rsrc.EditableMovieGroupLayerConf;
import flashbang.anim.rsrc.MovieConf;

public class LayerEditor extends Elements<LayerEditor>
{
    public LayerEditor (final EditableMovieConf conf) {
        super(AxisLayout.horizontal());
        add(_add, _remove, _indent, _dedent);
        _add.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                conf.add(conf.root, new EditableMovieGroupLayerConf("Group"));
            }
        });
    }

    protected final Button _add = new Button("+"), _remove = new Button("-"),
        _indent = new Button(">"), _dedent = new Button("<");
}
