//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import react.RList;
import react.Signal;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Elements;
import tripleplay.ui.Group;
import tripleplay.ui.Label;

import flashbang.anim.rsrc.EditableLayerAnimation;
import flashbang.anim.rsrc.EditableModelAnimation;
import flashbang.anim.rsrc.EditableModelResource;
import flashbang.anim.rsrc.Keyframe;
import flashbang.anim.rsrc.LayerDesc;
import flashbang.anim.rsrc.ModelAnimation;
import flashbang.anim.rsrc.ModelResource;

public class LayerTree extends Elements<LayerTree>
{
    public final Signal<Integer> frameSelected = Signal.create();

    public LayerTree (EditableModelResource resource, EditableModelAnimation animation) {
        super(AxisLayout.vertical());
        _resource = resource;
        _anim = animation;

        for (LayerDesc layer : resource.layers()) {
            // TODO - check for an existing LayerAnimation
            addLayer(layer);
        }
        resource.layers.listen(new RList.Listener<LayerDesc> () {
            @Override public void onAdd (LayerDesc layer) {
                addLayer(layer);
            }
        });
    }

    protected void addLayer (LayerDesc layer) {
        // TODO - layer nesting
        EditableLayerAnimation anim = new EditableLayerAnimation(layer.name);

        _anim.layers.add(anim);
        add(new Group(AxisLayout.horizontal()).
            add(new Label(layer.name), new KeyframeDisplay(anim, frameSelected)));
    }

    protected final EditableModelAnimation _anim;
    protected final ModelResource _resource;
}
