/*
 * @test
 * @summary javax.swing.ToolTipManager conformance: type shape + delay/registration contract (headless-safe).
 * @run main ToolTipManagerTest
 */

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class ToolTipManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.ToolTipManager".replace("javax.", "javax.swing."), false,
                ToolTipManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        // JDK-27 difference vs JDK-8: ToolTipManager is final here
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null
                && c.getSuperclass().getName().equals("java.awt.event.MouseAdapter"), "superclass");
        check(hasInterface(c, "java.awt.event.MouseMotionListener"), "implements java.awt.event.MouseMotionListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setEnabled"), "method setEnabled");
        check(names.contains("isEnabled"), "method isEnabled");
        check(names.contains("setLightWeightPopupEnabled"), "method setLightWeightPopupEnabled");
        check(names.contains("isLightWeightPopupEnabled"), "method isLightWeightPopupEnabled");
        check(names.contains("setInitialDelay"), "method setInitialDelay");
        check(names.contains("getInitialDelay"), "method getInitialDelay");
        check(names.contains("setDismissDelay"), "method setDismissDelay");
        check(names.contains("getDismissDelay"), "method getDismissDelay");
    }

    static void testBehavior() {
        ToolTipManager m = ToolTipManager.sharedInstance();
        check(m != null, "sharedInstance nonnull");
        check(m == ToolTipManager.sharedInstance(), "sharedInstance is a singleton");
        check(m instanceof MouseListener, "shared instance is a MouseListener");
        check(m instanceof MouseMotionListener, "shared instance is a MouseMotionListener");
        check(m.getInitialDelay() == 750, "initialDelay default 750");
        check(m.getDismissDelay() == 4000, "dismissDelay default 4000");
        check(m.getReshowDelay() == 500, "reshowDelay default 500");
        check(m.isEnabled(), "enabled by default");
        check(m.isLightWeightPopupEnabled(), "lightweight popups by default");
        m.setInitialDelay(777);
        check(m.getInitialDelay() == 777, "setInitialDelay");
        m.setDismissDelay(8888);
        check(m.getDismissDelay() == 8888, "setDismissDelay");
        m.setReshowDelay(999);
        check(m.getReshowDelay() == 999, "setReshowDelay");
        m.setEnabled(false);
        check(!m.isEnabled(), "setEnabled(false)");
        m.setEnabled(true);
        check(m.isEnabled(), "setEnabled(true)");
        m.setLightWeightPopupEnabled(false);
        check(!m.isLightWeightPopupEnabled(), "setLightWeightPopupEnabled(false)");
        m.setLightWeightPopupEnabled(true);
        check(m.isLightWeightPopupEnabled(), "setLightWeightPopupEnabled(true)");
        // restore documented defaults for other tests sharing the singleton
        m.setInitialDelay(750);
        m.setDismissDelay(4000);
        m.setReshowDelay(500);

        // Plain component (no tooltip text): register adds exactly one listener of each kind
        JLabel plain = new JLabel("y");
        check(plain.getListeners(MouseListener.class).length == 0, "plain component has no mouse listener yet");
        check(plain.getListeners(MouseMotionListener.class).length == 0,
                "plain component has no mouse-motion listener yet");
        m.registerComponent(plain);
        check(plain.getListeners(MouseListener.class).length == 1, "registerComponent adds mouse listener");
        check(plain.getListeners(MouseMotionListener.class).length == 1,
                "registerComponent adds mouse-motion listener");
        check(plain.getMouseListeners()[0] == m, "added mouse listener is the shared manager");
        m.unregisterComponent(plain);
        check(plain.getListeners(MouseListener.class).length == 0, "unregisterComponent removes mouse listener");
        check(plain.getListeners(MouseMotionListener.class).length == 0,
                "unregisterComponent removes mouse-motion listener");
        m.unregisterComponent(plain); // idempotent, no throw
        m.unregisterComponent(new JLabel("never-registered")); // no throw

        // Component with a tooltip: already auto-registered; explicit register is idempotent (remove+add same singleton)
        JLabel tipped = new JLabel("x");
        tipped.setToolTipText("tip");
        check(tipped.getListeners(MouseListener.class).length == 1,
                "setToolTipText auto-registers the shared manager");
        m.registerComponent(tipped);
        check(tipped.getListeners(MouseListener.class).length == 1,
                "registerComponent on auto-registered component stays at one listener");
        m.unregisterComponent(tipped);
        check(tipped.getListeners(MouseListener.class).length == 0,
                "unregisterComponent clears auto-registered listener");
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        testShape();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
