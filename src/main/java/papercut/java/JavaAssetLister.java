//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut.java;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import papercut.AssetLister;

import playn.core.PlayN;
import playn.core.ResourceCallback;
import playn.java.JavaAssetManager;

public class JavaAssetLister implements AssetLister
{
    public void listAssets (String directory, ResourceCallback<Iterable<String>> callback) {
        String root =
            new File(((JavaAssetManager)PlayN.assetManager()).getPathPrefix()).getAbsolutePath();
        List<String> assets = Lists.newArrayList();
        collectAssets(root, new File(root, directory), assets);
        callback.done(assets);

    }

    protected void collectAssets (String root, File dir, List<String> assets) {
        for (File sub : dir.listFiles()) {
            if (sub.isDirectory()) {
                collectAssets(root, sub, assets);
            } else {
                assets.add(sub.getAbsolutePath().substring(root.length()));
            }
        }
    }
}
