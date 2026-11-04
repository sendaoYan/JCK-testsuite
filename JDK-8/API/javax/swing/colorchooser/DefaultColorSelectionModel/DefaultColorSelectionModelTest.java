/*
 * @test
 * @summary javax.swing.colorchooser.DefaultColorSelectionModel conformance: type shape via
 *          reflection plus behavioral contract (events, null color, serialization, EDT).
 * @run main DefaultColorSelectionModelTest
 */

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DefaultColorSelectionModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        shape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() {
            try { behavior(); }
            catch (Exception e) { throw new AssertionError("unexpected checked exception", e); }
        }});
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- white-box: type shape (retained from skeleton) ----
    static void shape() throws Exception {
        Class<?> c = Class.forName("javax.swing.colorchooser.DefaultColorSelectionModel",
                false, DefaultColorSelectionModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "not abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "not final");
        boolean impl = false;
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals("javax.swing.colorchooser.ColorSelectionModel")) impl = true;
        check(impl, "implements ColorSelectionModel");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setSelectedColor"), "method setSelectedColor");
        check(names.contains("getSelectedColor"), "method getSelectedColor");
        check(names.contains("getChangeListeners"), "method getChangeListeners");
    }

    // ---- black-box: behavioral contract ----
    static void behavior() throws java.io.IOException, ClassNotFoundException {
        // named listener type keeps the model serializable
        DefaultColorSelectionModel m = new DefaultColorSelectionModel();
        check(Color.WHITE.equals(m.getSelectedColor()), "default selected color white");
        DefaultColorSelectionModel green = new DefaultColorSelectionModel(Color.GREEN);
        check(Color.GREEN.equals(green.getSelectedColor()), "explicit ctor color");

        // setSelectedColor roundtrip + event counting
        final int[] fired = {0};
        final Object[] src = {null};
        m.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { fired[0]++; src[0] = e.getSource(); }
        });
        m.setSelectedColor(Color.RED);
        check(Color.RED.equals(m.getSelectedColor()), "setSelectedColor roundtrip");
        check(fired[0] == 1, "different color fires exactly once");
        check(src[0] == m, "event source is the model");
        m.setSelectedColor(Color.RED);
        check(fired[0] == 1, "same color fires nothing");

        // setSelectedColor(null) is ignored: no change, no event, no throw
        m.setSelectedColor(null);
        check(Color.RED.equals(m.getSelectedColor()), "null color leaves selection unchanged");
        check(fired[0] == 1, "null color fires no event");

        // ctor(null) is allowed and yields a null selection
        DefaultColorSelectionModel n = new DefaultColorSelectionModel(null);
        check(n.getSelectedColor() == null, "ctor(null) -> null selection");
        final int[] nf = {0};
        n.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { nf[0]++; }
        });
        n.setSelectedColor(null);
        check(nf[0] == 0, "null->null on null-model fires nothing");
        // a null-initialized model throws NPE when set to a real color
        // (both JDK-8 and JDK-27 compare via the stored null selection)
        try { n.setSelectedColor(Color.BLUE);
              throw new AssertionError("expected NullPointerException on null-initialized model"); }
        catch (NullPointerException expected) { passed++; }

        // getChangeListeners bookkeeping
        ChangeListener a = new NopListener();
        ChangeListener b = new NopListener();
        DefaultColorSelectionModel g = new DefaultColorSelectionModel(Color.GRAY);
        check(g.getChangeListeners().length == 0, "no listeners initially");
        g.addChangeListener(a);
        check(g.getChangeListeners().length == 1, "one listener registered");
        g.addChangeListener(b);
        check(g.getChangeListeners().length == 2, "two listeners registered");
        g.removeChangeListener(a);
        check(g.getChangeListeners().length == 1, "listener removed");
        g.removeChangeListener(a);
        check(g.getChangeListeners().length == 1, "removing absent listener is a no-op");

        // removed listener no longer notified
        final int[] bf = {0};
        DefaultColorSelectionModel h = new DefaultColorSelectionModel(Color.YELLOW);
        ChangeListener tracked = new ChangeListener() { public void stateChanged(ChangeEvent e) { bf[0]++; } };
        h.addChangeListener(tracked);
        h.setSelectedColor(Color.ORANGE);
        h.removeChangeListener(tracked);
        h.setSelectedColor(Color.PINK);
        check(bf[0] == 1, "removed listener not notified");

        // serialization round-trip preserves the color, not the listeners
        DefaultColorSelectionModel ser = new DefaultColorSelectionModel(Color.GRAY);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(ser);
        oos.close();
        DefaultColorSelectionModel back = (DefaultColorSelectionModel)
                new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(Color.GRAY.equals(back.getSelectedColor()), "serialized color preserved");
        check(back.getChangeListeners().length == 0, "listeners not serialized");

        // polymorphic use through the interface
        ColorSelectionModel iface = new DefaultColorSelectionModel(Color.CYAN);
        check(Color.CYAN.equals(iface.getSelectedColor()), "usable as ColorSelectionModel");
    }

    static class NopListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {}
    }
}
