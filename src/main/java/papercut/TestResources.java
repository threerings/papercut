//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import flashbang.rsrc.ResourceFile;
import flashbang.util.Loadable;
import flashbang.util.LoadableBatch;

public class TestResources
{
    public static void load (Loadable.Callback callback) {
        if (_batch == null) {
            _batch = new LoadableBatch();
            _batch.add(new ResourceFile("streetwalker/streetwalker.json"));
        }
        _batch.load(callback);
    }

    protected static LoadableBatch _batch;
}
