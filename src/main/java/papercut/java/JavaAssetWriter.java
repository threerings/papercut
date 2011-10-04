//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut.java;

import java.io.File;
import java.io.IOException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import papercut.AssetWriter;

import playn.core.PlayN;
import playn.java.JavaAssetManager;

public class JavaAssetWriter implements AssetWriter
{
    public void write (String path, String text) {
        File out = new File(((JavaAssetManager)PlayN.assetManager()).getPathPrefix(), path);
        try {
            Files.write(text, out, Charsets.UTF_8);
        } catch (IOException io) {
            throw new RuntimeException(io);
        }
    }
}
