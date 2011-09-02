//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import com.threerings.flashbang.AppMode;
import com.threerings.flashbang.rsrc.anim.ModelResource;
import com.threerings.flashbang.anim.Model;
import com.threerings.flashbang.rsrc.anim.ModelResource;

public class AnimateMode extends AppMode
{
    @Override protected void setup ()
    {
        super.setup();

        Model model = new Model(ModelResource.require("streetwalker"));

        addObject(model, _modeLayer);
        model.setX(300);
        model.setY(200);
    }
}
