/*
 * @test
 * @summary javax.swing.JSeparator conformance: type shape via reflection + separator
 *          contract (orientation matrix/events, ctor IAE, UI delegate; headless-safe,
 *          Metal LaF, EDT).
 * @run main JSeparatorTest
 */

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JSeparatorTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkThrows(Class<? extends Throwable> ex, Runnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError(m + ": expected " + ex.getSimpleName() + " got " + t);
            passed++; return; }
        throw new AssertionError(m + ": expected " + ex.getSimpleName() + " not thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: structural shape (kept from the original skeleton) ----
    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.JSeparator", false, JSeparatorTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.swing.SwingConstants"), "implements javax.swing.SwingConstants");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("setUI"), "method setUI");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("getOrientation"), "method getOrientation");
        check(names.contains("setOrientation"), "method setOrientation");
    }

    // ---- Part 2: orientation matrix and property event ----
    static void testOrientation() {
        check(SwingConstants.HORIZONTAL == 0, "HORIZONTAL 0");
        check(SwingConstants.VERTICAL == 1, "VERTICAL 1");

        JSeparator s = new JSeparator();
        check(s.getOrientation() == SwingConstants.HORIZONTAL, "default orientation HORIZONTAL");
        check(s.isFocusable() == false, "not focusable");
        check("SeparatorUI".equals(s.getUIClassID()), "getUIClassID SeparatorUI");
        check("MetalSeparatorUI".equals(s.getUI().getClass().getSimpleName()), "Metal installs MetalSeparatorUI");
        check(s.getAccessibleContext() != null, "accessible context non-null");
        check(s.getInsets() != null, "insets non-null");

        JSeparator v = new JSeparator(SwingConstants.VERTICAL);
        check(v.getOrientation() == SwingConstants.VERTICAL, "VERTICAL ctor");

        // setOrientation round-trip and fired property event.
        final JSeparator e = new JSeparator();
        final AtomicReference<PropertyChangeEvent> ev = new AtomicReference<PropertyChangeEvent>();
        e.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent p) {
                if ("orientation".equals(p.getPropertyName())) ev.set(p);
            }
        });
        e.setOrientation(SwingConstants.VERTICAL);
        check(e.getOrientation() == SwingConstants.VERTICAL, "setOrientation(VERTICAL)");
        check(ev.get() != null, "setOrientation fires orientation event");
        check(((Integer) ev.get().getNewValue()).intValue() == SwingConstants.VERTICAL,
                "orientation event new value");
        e.setOrientation(SwingConstants.HORIZONTAL);
        check(e.getOrientation() == SwingConstants.HORIZONTAL, "setOrientation(HORIZONTAL)");

        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { new JSeparator(99); } },
                "JSeparator(99) ctor IAE");
        checkThrows(IllegalArgumentException.class,
                new Runnable() { public void run() { e.setOrientation(99); } },
                "setOrientation(99) IAE");
    }

    // ---- Part 3: generic component properties ----
    static void testProperties() {
        JSeparator s = new JSeparator();
        s.setMinimumSize(new Dimension(5, 5));
        check(new Dimension(5, 5).equals(s.getMinimumSize()), "minimum size round-trip");
        s.setMaximumSize(new Dimension(20, 20));
        check(new Dimension(20, 20).equals(s.getMaximumSize()), "maximum size round-trip");
        s.setPreferredSize(new Dimension(10, 10));
        check(new Dimension(10, 10).equals(s.getPreferredSize()), "preferred size round-trip");
        s.setEnabled(false);
        check(!s.isEnabled(), "disabled");
        s.setEnabled(true);
        check(s.isEnabled(), "enabled");
        s.setToolTipText("sep");
        check("sep".equals(s.getToolTipText()), "tooltip round-trip");
        s.setToolTipText(null);
        check(s.getToolTipText() == null, "tooltip null");
        s.setOpaque(true);
        check(s.isOpaque(), "opaque on");
        s.setName("sepname");
        check("sepname".equals(s.getName()), "name round-trip");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (Exception e) {
            throw new AssertionError("cannot install Metal LaF", e);
        }
        testShape();
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testOrientation(); } });
        SwingUtilities.invokeAndWait(new Runnable() { public void run() { testProperties(); } });
        System.out.println("PASS (" + passed + " assertions)");
    }
}
