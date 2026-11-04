/*
 * @test
 * @summary javax.swing.plaf.basic.BasicLookAndFeel conformance via a minimal concrete subclass (headless).
 * @run main BasicLookAndFeelContractTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.swing.JButton;
import javax.swing.UIDefaults;
import javax.swing.plaf.basic.BasicLookAndFeel;

/**
 * BasicLookAndFeel is the abstract shared engine of the Basic/Metal look-and-feels.
 * Locked here through a minimal concrete subclass (abstract hooks overridden with
 * fixed values): abstract-method surface, UIDefaults construction, lifecycle and
 * the LookAndFeel-level defaults implemented in this class.
 */
public class BasicLookAndFeelContractTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal concrete BasicLookAndFeel with fixed identity answers. */
    public static class ProbeBasic extends BasicLookAndFeel {
        public String getName() { return "ProbeBasicName"; }
        public String getID() { return "ProbeBasicID"; }
        public String getDescription() { return "ProbeBasicDesc"; }
        public boolean isNativeLookAndFeel() { return false; }
        public boolean isSupportedLookAndFeel() { return true; }
    }

    static void testAbstractSurface() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.basic.BasicLookAndFeel");
        check(Modifier.isAbstract(c.getModifiers()), "BasicLookAndFeel is abstract");
        check(c.getSuperclass().getName().equals("javax.swing.LookAndFeel"),
                "extends LookAndFeel");
        // the hooks every subclass must provide
        String[] abstractHooks = { "getID", "getName", "getDescription",
                "isNativeLookAndFeel", "isSupportedLookAndFeel" };
        for (String hook : abstractHooks) {
            Method m = c.getMethod(hook);
            check(Modifier.isAbstract(m.getModifiers()), hook + " remains abstract in BasicLookAndFeel");
        }
        // concrete services supplied by BasicLookAndFeel itself
        String[] concrete = { "getDefaults", "initialize", "uninitialize",
                "initClassDefaults", "initSystemColorDefaults", "initComponentDefaults" };
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (Method m : c.getDeclaredMethods()) declared.add(m.getName());
        for (String name : concrete) {
            check(declared.contains(name), name + " implemented by BasicLookAndFeel");
        }
    }

    static void testIdentityAndDefaults() {
        ProbeBasic lf = new ProbeBasic();
        check("ProbeBasicName".equals(lf.getName()), "getName from subclass");
        check("ProbeBasicID".equals(lf.getID()), "getID from subclass");
        check("ProbeBasicDesc".equals(lf.getDescription()), "getDescription from subclass");
        check(!lf.isNativeLookAndFeel(), "isNativeLookAndFeel false for probe");
        check(lf.isSupportedLookAndFeel(), "isSupportedLookAndFeel true for probe");
        // LookAndFeel.getSupportsWindowDecorations default is false (inherited, not overridden here)
        check(!lf.getSupportsWindowDecorations(), "window decorations unsupported by default");
        // toString = "[" + description + " - " + implementation class + "]"
        check(lf.toString().equals("[ProbeBasicDesc - " + ProbeBasic.class.getName() + "]"),
                "toString uses description and class name");

        UIDefaults d = lf.getDefaults();
        check(d != null && d.size() > 0, "getDefaults returns a populated table");
        // BasicLookAndFeel maps every component to its Basic UI delegate
        check("javax.swing.plaf.basic.BasicButtonUI".equals(d.get("ButtonUI")),
                "ButtonUI default is BasicButtonUI");
        check("javax.swing.plaf.basic.BasicLabelUI".equals(d.get("LabelUI")),
                "LabelUI default is BasicLabelUI");
        check(d.get("ScrollBarUI") != null && String.valueOf(d.get("ScrollBarUI")).contains("BasicScrollBarUI"),
                "ScrollBarUI default is BasicScrollBarUI");
        // initialize/uninitialize lifecycle is safe to run repeatedly
        lf.initialize();
        lf.initialize();
        check(true, "initialize() re-entrant");
        lf.uninitialize();
        check(true, "uninitialize() after initialize() does not throw");
    }

    /** MetalLookAndFeel is the canonical shipped concrete subclass. */
    static void testMetalIsABasic() throws Exception {
        Class<?> metal = Class.forName("javax.swing.plaf.metal.MetalLookAndFeel");
        check(metal.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicLookAndFeel"),
                "MetalLookAndFeel extends BasicLookAndFeel");
        javax.swing.plaf.metal.MetalLookAndFeel instance =
                new javax.swing.plaf.metal.MetalLookAndFeel();
        check(instance instanceof BasicLookAndFeel, "Metal instance is a BasicLookAndFeel");
        javax.swing.UIManager.setLookAndFeel(instance);
        JButton b = new JButton();
        b.updateUI();
        check(b.getUI() instanceof javax.swing.plaf.ButtonUI, "Metal-driven button UI is a ButtonUI");
        UIDefaults metalDefaults = instance.getDefaults();
        check(metalDefaults != null && metalDefaults.size() > 0, "Metal getDefaults populated");
    }

    public static void main(String[] args) throws Exception {
        testAbstractSurface();
        testIdentityAndDefaults();
        testMetalIsABasic();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
