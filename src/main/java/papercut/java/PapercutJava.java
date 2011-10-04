//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut.java;

import papercut.Papercut;
import papercut.PapercutApp;

import playn.core.PlayN;
import playn.java.JavaPlatform;

public class PapercutJava
{
    public static void main (String[] args) {
        JavaPlatform.register().assetManager().setPathPrefix("src/main/resources");
        Papercut.init(new JavaAssetLister(), new JavaAssetWriter());
        PlayN.run(new PapercutApp());
    }
}
