/*
 * @test
 * @summary javax.swing.UIManager conformance: type shape + look-and-feel switching + typed defaults (headless-safe).
 * @run main UIManagerTest
 */

import java.beans.PropertyChangeEvent;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLookAndFeel;

public class UIManagerTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Throwable checkThrows(Class<? extends Throwable> ex, Runnable r) {
        try { r.run(); } catch (Throwable t) {
            if (!ex.isInstance(t)) throw new AssertionError("expected " + ex.getSimpleName() + " got " + t);
            passed++; return t; }
        throw new AssertionError("expected " + ex.getSimpleName() + " not thrown");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.UIManager", false, UIManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(hasInterface(c, "java.io.Serializable"), "implements java.io.Serializable");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getInstalledLookAndFeels"), "method getInstalledLookAndFeels");
        check(names.contains("setInstalledLookAndFeels"), "method setInstalledLookAndFeels");
        check(names.contains("installLookAndFeel"), "method installLookAndFeel");
        check(names.contains("getLookAndFeel"), "method getLookAndFeel");
        check(names.contains("setLookAndFeel"), "method setLookAndFeel");
    }

    static void testLookAndFeelSwitching() throws Exception {
        MetalLookAndFeel metal = new MetalLookAndFeel();
        UIManager.setLookAndFeel(metal);
        check(UIManager.getLookAndFeel() == metal, "getLookAndFeel returns instance set");
        check("Metal".equals(UIManager.getLookAndFeel().getName()), "Metal active");
        final List<PropertyChangeEvent> evts = new ArrayList<PropertyChangeEvent>();
        UIManager.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) { evts.add(e); }
        });
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        check(evts.size() == 1 && "lookAndFeel".equals(evts.get(0).getPropertyName()),
                "setLookAndFeel fires lookAndFeel event");
        check(evts.get(0).getNewValue() instanceof MetalLookAndFeel,
                "event carries new LookAndFeel");
        check(UIManager.getLookAndFeel() instanceof MetalLookAndFeel, "still Metal after switch");
        // String overload
        UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        check(UIManager.getLookAndFeel() instanceof MetalLookAndFeel, "setLookAndFeel(String)");
        boolean threwCNFE = false;
        try {
            UIManager.setLookAndFeel("com.bogus.BogusLookAndFeel");
        } catch (ClassNotFoundException e) {
            threwCNFE = true;
        } catch (Exception e) {
            throw new AssertionError("unexpected exception from bogus LAF", e);
        }
        check(threwCNFE, "setLookAndFeel(bogus) throws ClassNotFoundException");
        check(UIManager.getLookAndFeel() instanceof MetalLookAndFeel,
                "failed setLookAndFeel leaves current LAF intact");
        check("javax.swing.plaf.metal.MetalLookAndFeel".equals(
                UIManager.getCrossPlatformLookAndFeelClassName()),
                "cross-platform LAF is Metal");
    }

    static void testInstalledLookAndFeels() {
        LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
        check(infos != null && infos.length >= 2, "at least two installed LAFs");
        boolean metal = false, nimbus = false;
        String metalName = null, metalClass = null;
        for (LookAndFeelInfo i : infos) {
            if ("Metal".equals(i.getName())) {
                metal = true;
                metalName = i.getName();
                metalClass = i.getClassName();
            }
            if ("Nimbus".equals(i.getName())) nimbus = true;
        }
        check(metal, "Metal installed");
        check(nimbus, "Nimbus installed");
        check("Metal".equals(metalName), "LookAndFeelInfo name");
        check("javax.swing.plaf.metal.MetalLookAndFeel".equals(metalClass),
                "LookAndFeelInfo class name");
    }

    static void testDefaults() {
        UIDefaults d = UIManager.getDefaults();
        check(d instanceof UIDefaults, "getDefaults type");
        check(UIManager.getDefaults() == d, "getDefaults stable instance");
        UIManager.put("probe.key", "probeValue");
        check("probeValue".equals(UIManager.get("probe.key")), "UIManager.put/get");
        check("probeValue".equals(UIManager.getString("probe.key")),
                "getString reads UIManager table");
        check("Yes".equals(UIManager.getString("OptionPane.yesButtonText")),
                "OptionPane.yesButtonText is Yes");
        check(UIManager.getFont("Label.font") != null, "Label.font nonnull");
        java.awt.Color focus = UIManager.getColor("Table.focusCellBackground");
        check(focus != null && focus.getRed() == 255 && focus.getGreen() == 255 && focus.getBlue() == 255,
                "Table.focusCellBackground is white");
        check(UIManager.getBorder("TextField.border") != null, "TextField.border nonnull");
        check(UIManager.getIcon("Tree.openIcon") != null, "Tree.openIcon nonnull");
        java.awt.Insets tab = UIManager.getInsets("TabbedPane.tabInsets");
        check(tab != null && tab.top == 0 && tab.left == 9 && tab.bottom == 1 && tab.right == 9,
                "TabbedPane.tabInsets exact values");
        java.awt.Dimension min = UIManager.getDimension("OptionPane.minimumSize");
        check(min != null && min.width == 262 && min.height == 90,
                "OptionPane.minimumSize exact values");
        check(UIManager.getBoolean("ScrollBar.allowsAbsolutePositioning"),
                "ScrollBar.allowsAbsolutePositioning true");
        ComponentUI ui = UIManager.getUI(new JLabel("x"));
        check(ui != null && "javax.swing.plaf.metal.MetalLabelUI".equals(ui.getClass().getName()),
                "getUI(JLabel) yields MetalLabelUI under Metal");
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ENGLISH);
        testShape();
        testLookAndFeelSwitching();
        testInstalledLookAndFeels();
        testDefaults();
        // setLookAndFeel(null) is accepted without throwing but poisons the UI:
        // keep it last and restore Metal immediately afterwards.
        UIManager.setLookAndFeel((javax.swing.LookAndFeel) null);
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        System.out.println("PASS (" + passed + " assertions)");
    }
}
