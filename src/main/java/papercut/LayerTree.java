//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import react.RList;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Elements;
import tripleplay.ui.Group;
import tripleplay.ui.Label;

import flashbang.anim.rsrc.LayerDesc;
import flashbang.anim.rsrc.ModelResource;

public class LayerTree extends Elements<LayerTree>
{
    public LayerTree (ModelResource resource) {
        super(AxisLayout.vertical().alignLeft());
        _resource = resource;

        for (LayerDesc layer : resource.layers) {
            addLayer(layer);
        }
        resource.layers.listen(new RList.Listener<LayerDesc> () {
            @Override public void onAdd (LayerDesc layer) {
                addLayer(layer);
            }
        });
    }

    protected void addLayer (LayerDesc layer) {
        Group container = new Group(AxisLayout.horizontal().alignOn(AxisLayout.Align.START));
        container.add(new Label(layer.name), new KeyframeDisplay());
        add(container);
    }

    protected final ModelResource _resource;
}
