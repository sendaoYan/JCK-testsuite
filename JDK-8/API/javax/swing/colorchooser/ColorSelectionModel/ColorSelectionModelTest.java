/*
 * @test
 * @summary javax.swing.colorchooser.ColorSelectionModel conformance: interface shape via
 *          reflection plus behavioral contract (minimal implementation, EDT).
 * @run main ColorSelectionModelTest
 */

import java.awt.Color;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ColorSelectionModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        shape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { behavior(); }});
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- white-box: interface shape (retained from skeleton) ----
    static void shape() throws Exception {
        Class<?> c = Class.forName("javax.swing.colorchooser.ColorSelectionModel",
                false, ColorSelectionModelTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "interface method abstract: " + m.getName());
            names.add(m.getName());
        }
        check(names.contains("getSelectedColor"), "method getSelectedColor");
        check(names.contains("setSelectedColor"), "method setSelectedColor");
        check(names.contains("addChangeListener"), "method addChangeListener");
        check(names.contains("removeChangeListener"), "method removeChangeListener");
        check(c.getMethods().length == 4, "exactly four interface methods");
    }

    // ---- black-box: behavioral contract via a minimal implementation ----
    static final class MinimalModel implements javax.swing.colorchooser.ColorSelectionModel {
        Color color = Color.RED;
        final java.util.List<ChangeListener> listeners = new java.util.ArrayList<ChangeListener>();
        public Color getSelectedColor() { return color; }
        public void setSelectedColor(Color c) {
            if (c == null || c.equals(color)) return;
            color = c;
            for (ChangeListener l : listeners) l.stateChanged(new ChangeEvent(this));
        }
        public void addChangeListener(ChangeListener l) { listeners.add(l); }
        public void removeChangeListener(ChangeListener l) { listeners.remove(l); }
    }

    static void behavior() {
        javax.swing.colorchooser.ColorSelectionModel m = new MinimalModel();
        check(m instanceof javax.swing.colorchooser.ColorSelectionModel,
              "minimal impl assignable to interface");
        check(Color.RED.equals(m.getSelectedColor()), "getSelectedColor initial value");
        m.setSelectedColor(Color.BLUE);
        check(Color.BLUE.equals(m.getSelectedColor()), "setSelectedColor updates color");

        final int[] fired = {0};
        final Object[] source = {null};
        ChangeListener l = new ChangeListener() {
            public void stateChanged(ChangeEvent e) { fired[0]++; source[0] = e.getSource(); }
        };
        m.addChangeListener(l);
        m.setSelectedColor(Color.GREEN);
        check(fired[0] == 1, "setSelectedColor fires registered listener");
        check(source[0] == m, "change event source is the model");
        m.setSelectedColor(Color.GREEN);
        check(fired[0] == 1, "same color fires nothing");
        m.removeChangeListener(l);
        m.setSelectedColor(Color.BLACK);
        check(fired[0] == 1, "removed listener not notified");

        // two listeners both notified
        final int[] a = {0}, b = {0};
        ChangeListener la = new ChangeListener() { public void stateChanged(ChangeEvent e) { a[0]++; } };
        ChangeListener lb = new ChangeListener() { public void stateChanged(ChangeEvent e) { b[0]++; } };
        m.addChangeListener(la);
        m.addChangeListener(lb);
        m.setSelectedColor(Color.WHITE);
        check(a[0] == 1 && b[0] == 1, "all registered listeners notified");

        // interop: a custom model drives JColorChooser.getColor()
        javax.swing.JColorChooser cc = new javax.swing.JColorChooser(m);
        check(cc.getColor().equals(Color.WHITE), "JColorChooser reads custom model color");
        m.setSelectedColor(Color.CYAN);
        check(cc.getColor().equals(Color.CYAN), "model changes propagate to chooser getColor");
    }
}
