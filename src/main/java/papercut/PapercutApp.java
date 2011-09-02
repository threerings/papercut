//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import playn.core.PlayN;
import com.threerings.flashbang.util.Loadable;
import com.threerings.flashbang.Flashbang;
import pythagoras.i.Point;
import com.threerings.flashbang.FlashbangApp;

public class PapercutApp extends FlashbangApp
{
    @Override public void init ()
    {
        super.init();

        // Load resources
        TestResources.load(new Loadable.Callback() {
            @Override public void done () {
                Flashbang.app().defaultViewport().unwindToMode(new AnimateMode());
            }
            @Override public void error (Throwable err) {
                PlayN.log().error("Something broke!", err);
            }
        });

    }

    @Override public int updateRate ()
    {
        return UPDATE_RATE;
    }

    @Override public Point screenSize ()
    {
        return SCREEN_SIZE;
    }

    protected static final int UPDATE_RATE = 30;
    protected static final Point SCREEN_SIZE = new Point(640, 960);
}
