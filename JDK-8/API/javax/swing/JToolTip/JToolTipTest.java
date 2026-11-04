/*
 * @test
 * @summary javax.swing.JToolTip conformance: type shape via reflection + component-layer
 *          black-box contracts (tip/component null-safe roundtrips, UI binding, accessibility).
 * @run main JToolTipTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JToolTip;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class JToolTipTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkEquals(Object expect, Object actual, String m) {
        check(expect == null ? actual == null : expect.equals(actual),
              m + ": expected " + expect + " but got " + actual);
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        final Class<?> c = Class.forName("javax.swing.JToolTip", false, JToolTipTest.class.getClassLoader());
        EventQueue.invokeAndWait(new Runnable() { public void run() { testShape(c); }});
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        EventQueue.invokeAndWait(new Runnable() { public void run() { testContracts(); }});
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- type shape (reflection, no construction) ----
    static void testShape(Class<?> c) {
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.JComponent"), "superclass");
        check(hasInterface(c, "javax.accessibility.Accessible"), "implements javax.accessibility.Accessible");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getUI"), "method getUI");
        check(names.contains("updateUI"), "method updateUI");
        check(names.contains("getUIClassID"), "method getUIClassID");
        check(names.contains("setTipText"), "method setTipText");
        check(names.contains("getTipText"), "method getTipText");
        check(names.contains("setComponent"), "method setComponent");
        check(names.contains("getComponent"), "method getComponent");
    }

    // ---- behavioral contracts ----
    static void testContracts() {
        JToolTip tip = new JToolTip();
        check(tip.getTipText() == null, "default tipText null");
        check(tip.getComponent() == null, "default component null");

        tip.setTipText("tip");
        checkEquals("tip", tip.getTipText(), "setTipText roundtrip");
        tip.setTipText("second");
        checkEquals("second", tip.getTipText(), "setTipText overwrite");
        tip.setTipText(null);
        check(tip.getTipText() == null, "setTipText(null) clears, no NPE");

        JButton b = new JButton("host");
        tip.setComponent(b);
        check(tip.getComponent() == b, "setComponent identity");
        tip.setComponent(null);
        check(tip.getComponent() == null, "setComponent(null) clears, no NPE");

        check(tip.getUI() != null && tip.getUI().getClass().getName().endsWith("MetalToolTipUI"),
              "Metal binding installs MetalToolTipUI");
        checkEquals("ToolTipUI", tip.getUIClassID(), "getUIClassID ToolTipUI");
        check(tip.getAccessibleContext() != null, "getAccessibleContext non-null");
        // setToolTipText is the JComponent-level property, distinct from setTipText
        tip.setToolTipText("x");
        check(tip.getTipText() == null, "setToolTipText does not feed getTipText");
        checkEquals("x", tip.getToolTipText(), "JComponent tooltip property roundtrip");
        tip.updateUI();
        check(tip.getUI() != null, "updateUI rebinds UI no-throw");
    }
}
