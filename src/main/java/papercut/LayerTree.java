//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import react.RList;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Elements;
import tripleplay.ui.Label;

import flashbang.anim.rsrc.LayerDesc;
import flashbang.anim.rsrc.ModelResource;

public class LayerTree extends Elements<LayerTree>
{
    public LayerTree (ModelResource resource) {
        super(AxisLayout.vertical().alignLeft());
        for (LayerDesc layer : resource.layers) {
            add(new Label(layer.name));
        }
        resource.layers.listen(new RList.Listener<LayerDesc> () {
            @Override public void onAdd (LayerDesc layer) {
                add(new Label(layer.name));
            }
        });
    }
}
