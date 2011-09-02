//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut.java;

import playn.core.PlayN;
import playn.java.JavaPlatform;
import papercut.PapercutApp;

public class Papercut
{
    public static void main (String[] args) {
        JavaPlatform.register().assetManager().setPathPrefix("src/main/resources");
        PlayN.run(new PapercutApp());
    }
}
