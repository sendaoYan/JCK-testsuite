/*
 * @test
 * @summary javax.swing.plaf.nimbus.NimbusLookAndFeel conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main NimbusLookAndFeelTest
 */

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.lang.reflect.Modifier;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.UIDefaults;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import javax.swing.plaf.nimbus.NimbusStyle;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyleFactory;

public class NimbusLookAndFeelTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(NimbusLookAndFeelTest::testWhiteBox);
        runOnEDT(NimbusLookAndFeelTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.synth.SynthLookAndFeel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        NimbusLookAndFeel laf = new NimbusLookAndFeel();
        SynthStyleFactory old = SynthLookAndFeel.getStyleFactory();
        laf.initialize();
        try {
            check("Nimbus".equals(laf.getName()), "getName");
            check("Nimbus".equals(laf.getID()), "getID");
            check("Nimbus Look and Feel".equals(laf.getDescription()), "getDescription");
            check(laf.isNativeLookAndFeel() == false, "isNativeLookAndFeel false");
            check(laf.isSupportedLookAndFeel() == true, "isSupportedLookAndFeel true");

            UIDefaults defs = laf.getDefaults();
            check(defs != null && defs.size() > 0, "getDefaults non-empty");

            Color derived = laf.getDerivedColor("nimbusBase", 0.1f, 0.2f, 0.3f, 10, true);
            check(derived != null, "getDerivedColor non-null");

            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Icon icon = new ImageIcon(img);
            JButton b = new JButton();
            Icon disabled = laf.getDisabledIcon(b, icon);
            check(disabled != null && disabled != icon, "getDisabledIcon returns derived icon");

            Region custom = new Region("CustomRegion", "CustomUI", false) {};
            laf.register(custom, "CustomPrefix");
            check(true, "register completes");

            NimbusStyle style = NimbusLookAndFeel.getStyle(b, Region.BUTTON);
            check(style != null && style instanceof NimbusStyle, "getStyle returns NimbusStyle");
        } finally {
            laf.uninitialize();
            SynthLookAndFeel.setStyleFactory(old);
        }
    }
}
