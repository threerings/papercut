//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import playn.core.PlayN;
import playn.core.ResourceCallback;

import pythagoras.i.Point;

import flashbang.Flashbang;
import flashbang.FlashbangApp;
import flashbang.rsrc.ImageResource;
import flashbang.rsrc.JsonResource;
import flashbang.rsrc.ResourceBatch;
import flashbang.util.Loadable;

public class PapercutApp extends FlashbangApp
{
    public PapercutApp (LocalAssets assets) {
        _assets = assets;
    }

    @Override public void init () {
        super.init();

        _assets.listAssets("", new ResourceCallback<Iterable<String>>() {
            @Override public void done (Iterable<String> assets) {
                final Iterable<String> pngs = Iterables.filter(assets, new Predicate<String> () {
                    @Override public boolean apply (String asset) {
                        return asset.endsWith(".png");
                    }
                });
                ResourceBatch images = new ResourceBatch("pngs");
                for (String png : pngs) {
                    images.add(new ImageResource(png));
                }
                images.add(new JsonResource("streetwalker/streetwalker.json"));
                images.load(new Loadable.Callback () {
                    @Override public void done () {
                        Flashbang.app().defaultViewport().unwindToMode(new AnimateMode(pngs,
                            _assets));
                    }

                    @Override public void error (Throwable t) {
                        PlayN.log().error("Loading pngs failed!", t);
                    }
                });
            }
            @Override public void error (Throwable t) {
                PlayN.log().error("Listing failed!", t);
            }
        });
    }

    @Override public Point viewSize () { return SCREEN_SIZE; }

    protected final LocalAssets _assets;

    protected static final Point SCREEN_SIZE = new Point(1024, 768);
}
