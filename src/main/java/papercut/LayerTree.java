//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import react.RList;
import react.Signal;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Elements;
import tripleplay.ui.Group;
import tripleplay.ui.Label;

import flashbang.anim.rsrc.KeyframeDesc;
import flashbang.anim.rsrc.LayerAnimDesc;
import flashbang.anim.rsrc.LayerDesc;
import flashbang.anim.rsrc.ModelAnimDesc;
import flashbang.anim.rsrc.ModelResource;

public class LayerTree extends Elements<LayerTree>
{
    public final Signal<KeyframeDesc> frameSelected = Signal.create();

    public LayerTree (ModelResource resource, ModelAnimDesc animation) {
        super(AxisLayout.vertical());
        _resource = resource;
        _anim = animation;

        for (LayerDesc layer : resource.layers) {
            // TODO - check for an existing LayerAnimDesc
            addLayer(layer);
        }
        resource.layers.listen(new RList.Listener<LayerDesc> () {
            @Override public void onAdd (LayerDesc layer) {
                addLayer(layer);
            }
        });
    }

    protected void addLayer (LayerDesc layer) {
        LayerAnimDesc anim = new LayerAnimDesc();
        anim.layerSelector = layer.name;
        anim.keyframes.add(new KeyframeDesc());

        _anim.layerAnims.add(anim);
        add(new Group(AxisLayout.horizontal()).
            add(new Label(layer.name), new KeyframeDisplay(anim, frameSelected)));
    }

    protected final ModelAnimDesc _anim;
    protected final ModelResource _resource;
}
