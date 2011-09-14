//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.PlayN;
import playn.core.ResourceCallback;

import pythagoras.i.Point;

import com.threerings.flashbang.Flashbang;
import com.threerings.flashbang.FlashbangApp;

public class PapercutApp extends FlashbangApp
{
    @Override public void init () {
        super.init();

        Papercut.listAssets("", new ResourceCallback<Iterable<String>>() {
            @Override public void done (Iterable<String> assets) {
                Flashbang.app().defaultViewport().unwindToMode(new AnimateMode(assets));
            }
            @Override public void error (Throwable t) {
                PlayN.log().error("Listing failed!", t);
            }
        });
    }

    @Override public Point screenSize () {
        return SCREEN_SIZE;
    }

    protected static final Point SCREEN_SIZE = new Point(1024, 768);
}
