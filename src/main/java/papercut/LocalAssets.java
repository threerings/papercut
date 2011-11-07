//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import playn.core.ResourceCallback;

public class LocalAssets
{
    public LocalAssets (String pathPrefix) {
        _pathPrefix = pathPrefix;
    }

    public void write (String path, String text) {
        File out = new File(_pathPrefix, path);
        try {
            Files.write(text, out, Charsets.UTF_8);
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }

    public void listAssets (String directory, ResourceCallback<Iterable<String>> callback) {
        String root = new File(_pathPrefix).getAbsolutePath();
        List<String> assets = Lists.newArrayList();
        collectAssets(root, new File(root, directory), assets);
        callback.done(assets);
    }

    protected void collectAssets (String root, File dir, List<String> assets) {
        for (File sub : dir.listFiles()) {
            if (sub.isDirectory()) {
                collectAssets(root, sub, assets);
            } else {
                assets.add(sub.getAbsolutePath().substring(root.length() + 1));
            }
        }
    }

    protected final String _pathPrefix;
}
