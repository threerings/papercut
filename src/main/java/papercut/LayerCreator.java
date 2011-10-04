//
// Papercut - Copyright 2011 Three Rings Design, Inc.

package papercut;

import java.lang.Iterable;

import playn.core.GroupLayer;

import react.Slot;

import tripleplay.ui.AxisLayout;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Interface;
import tripleplay.ui.Root;
import tripleplay.ui.Selector;
import tripleplay.ui.Styles;

import flashbang.anim.rsrc.EditableMovieConf;
import flashbang.anim.rsrc.EditableMovieGroupLayerConf;
import flashbang.anim.rsrc.EditableMovieImageLayerConf;

import static tripleplay.ui.Style.*;

public class LayerCreator
{
    public LayerCreator (final Interface iface, final GroupLayer parent, Iterable<String> images,
        final EditableMovieConf movie, final LayerTree tree, float xCenter, float yBottom) {
        final Root root = iface.createRoot(AxisLayout.vertical().gap(0), AnimateMode.ROOT, parent).
            setStyles(Styles.make(HALIGN.left, VALIGN.top));
        for (String image : images) {
            root.add(new Button(image));
        }
        final Button newGroup, cancel;
        root.add((newGroup = new Button("New Group")));
        root.add((cancel = new Button("Cancel")));
        root.packToWidth(WIDTH);
        float height = root.size().height();
        root.setBounds(xCenter, yBottom - height, WIDTH, height);
        new Selector().add(root).selected.connect(new Slot<Element<?>> () {
            @Override public void onEmit (Element<?> selected) {
                System.out.println("CLIECED " + selected);
                if (selected == newGroup) {
                    movie.add(tree.groupLayer(), new EditableMovieGroupLayerConf("Group"));
                } else if (selected != cancel) {
                    String image = ((Button)selected).text();
                    movie.add(tree.groupLayer(), new EditableMovieImageLayerConf(image));
                }
                iface.removeRoot(root);
            }
        });
    }

    protected static final int WIDTH = 200, HEIGHT = 400;
}
