//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.ResourceCallback;

public class Papercut
{
    public static void init (AssetLister lister, AssetWriter writer) {
        _lister = lister;
        _writer = writer;
    }

    public static void listAssets (String prefix, ResourceCallback<Iterable<String>> callback) {
        _lister.listAssets(prefix, callback);
    }

    public static void write (String path, String text) {
        _writer.write(path, text);
    }

    protected static AssetLister _lister;
    protected static AssetWriter _writer;
}
