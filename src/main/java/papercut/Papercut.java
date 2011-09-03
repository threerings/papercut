//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.ResourceCallback;

public class Papercut
{
    public static void init (AssetLister lister) {
        _lister = lister;
    }

    public static void listAssets (String prefix, ResourceCallback<Iterable<String>> callback) {
        _lister.listAssets(prefix, callback);
    }

    protected static AssetLister _lister;
}
