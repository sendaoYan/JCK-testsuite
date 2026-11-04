/*
 * @test
 * @summary javax.swing.plaf.nimbus.NimbusLookAndFeel conformance: type shape via reflection (headless-safe).
 * @run main NimbusLookAndFeelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class NimbusLookAndFeelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.NimbusLookAndFeel", false, NimbusLookAndFeelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.synth.SynthLookAndFeel"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("initialize"), "method initialize");
        check(names.contains("uninitialize"), "method uninitialize");
        check(names.contains("getDefaults"), "method getDefaults");
        check(names.contains("getStyle"), "method getStyle");
        check(names.contains("getName"), "method getName");
        check(names.contains("getID"), "method getID");
        check(names.contains("getDescription"), "method getDescription");
        testLegacyShape();
        testMetadata();
        testDerivedColors();
        testStyleAndRegistration();
        testInstalledBehaviour();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 2: identity metadata ----
    static void testMetadata() throws Exception {
        javax.swing.plaf.nimbus.NimbusLookAndFeel lf = new javax.swing.plaf.nimbus.NimbusLookAndFeel();
        check("Nimbus".equals(lf.getName()), "getName() == \"Nimbus\"");
        check("Nimbus".equals(lf.getID()), "getID() == \"Nimbus\"");
        check("Nimbus Look and Feel".equals(lf.getDescription()), "getDescription()");
        check(lf.isSupportedLookAndFeel(), "supported on all platforms");
        check(!lf.isNativeLookAndFeel(), "not native");
        // Nimbus delegates its chrome painting, so it does not need LaF window decorations
        check(!lf.getSupportsWindowDecorations(), "no window decorations");
        check(lf instanceof javax.swing.plaf.synth.SynthLookAndFeel, "built on Synth");
        check(lf.shouldUpdateStyleOnAncestorChanged(), "styles keyed on ancestors");
    }

    // ---- Part 3: derived colors ----
    static void testDerivedColors() throws Exception {
        javax.swing.plaf.nimbus.NimbusLookAndFeel lf = new javax.swing.plaf.nimbus.NimbusLookAndFeel();
        java.awt.Color dc = lf.getDerivedColor("white", 1.0f, 1.0f, 1.0f, 0, true);
        check(dc != null, "getDerivedColor non-null");
        check(dc.getAlpha() >= 0 && dc.getAlpha() <= 255, "derived color alpha in range");
        // same inputs cache to the same instance
        java.awt.Color dc2 = lf.getDerivedColor("white", 1.0f, 1.0f, 1.0f, 0, true);
        check(dc == dc2, "derived colors are cached per LaF instance");
        // uiResource=false variant is a distinct instance
        java.awt.Color dc3 = lf.getDerivedColor("white", 1.0f, 1.0f, 1.0f, 0, false);
        check(dc3 != null && dc3 != dc, "non-UIResource variant is a distinct instance");
    }

    // ---- Part 4: style lookup + region registration ----
    static void testStyleAndRegistration() throws Exception {
        // register re-points a region's style prefix without throwing
        javax.swing.plaf.nimbus.NimbusLookAndFeel lf = new javax.swing.plaf.nimbus.NimbusLookAndFeel();
        lf.register(javax.swing.plaf.synth.Region.BUTTON, "Button[Disabled]");
        check(true, "register(Region, prefix) accepted");
        // getDisabledIcon is inherited from Synth: plain icons pass through or filter, never null-crash
        javax.swing.Icon plain = javax.swing.plaf.metal.MetalIconFactory.getCheckBoxIcon();
        Object disabled = lf.getDisabledIcon(new javax.swing.JButton(), plain);
        check(true, "getDisabledIcon tolerated headless");
    }

    // ---- Part 5: installed behaviour (headless) ----
    static void testInstalledBehaviour() throws Exception {
        javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.nimbus.NimbusLookAndFeel());
        javax.swing.JButton b = new javax.swing.JButton("x");
        b.updateUI();
        check(b.getUI() instanceof javax.swing.plaf.synth.SynthUI, "Nimbus delegates are SynthUIs");
        check(b.getUI().getClass().getName().equals("javax.swing.plaf.synth.SynthButtonUI"),
                "button delegate is SynthButtonUI under Nimbus");
        check(!b.isOpaque(), "Nimbus buttons are non-opaque (painted by regions)");
        javax.swing.UIDefaults d = javax.swing.UIManager.getLookAndFeel().getDefaults();
        check(d != null && d.size() > 100, "Nimbus defaults heavily populated");
        check(d.get("ButtonUI") != null, "ButtonUI present in Nimbus defaults");
        // static style lookup resolves through the installed Nimbus factory
        Object style = javax.swing.plaf.nimbus.NimbusLookAndFeel.getStyle(
                b, javax.swing.plaf.synth.Region.BUTTON);
        check(style != null, "getStyle(Button) non-null when installed");
        check(style instanceof javax.swing.plaf.nimbus.NimbusStyle, "style is a NimbusStyle");
        Object labelStyle = javax.swing.plaf.nimbus.NimbusLookAndFeel.getStyle(
                new javax.swing.JLabel(), javax.swing.plaf.synth.Region.LABEL);
        check(labelStyle != null, "label style non-null when installed");
        // back to Metal for other tests in the same JVM
        javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
        javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(
                new javax.swing.plaf.metal.OceanTheme());
    }

    // ---- Part 6: legacy shallow coverage (retained verbatim) ----
    static void testLegacyShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.NimbusLookAndFeel", false, NimbusLookAndFeelTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.synth.SynthLookAndFeel"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("initialize"), "method initialize");
        check(names.contains("uninitialize"), "method uninitialize");
        check(names.contains("getDefaults"), "method getDefaults");
        check(names.contains("getStyle"), "method getStyle");
        check(names.contains("getName"), "method getName");
        check(names.contains("getID"), "method getID");
        check(names.contains("getDescription"), "method getDescription");
    }
}
