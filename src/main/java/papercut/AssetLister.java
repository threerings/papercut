//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.ResourceCallback;

public interface AssetLister
{
    void listAssets (String directory, ResourceCallback<Iterable<String>> callback);
}
