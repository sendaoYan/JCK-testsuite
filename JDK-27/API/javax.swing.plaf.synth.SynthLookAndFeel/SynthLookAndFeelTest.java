/*
 * @test
 * @summary javax.swing.plaf.synth.SynthLookAndFeel conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main SynthLookAndFeelTest
 */

import java.awt.EventQueue;
import java.lang.reflect.Modifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthButtonUI;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;

public class SynthLookAndFeelTest {
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
        runOnEDT(SynthLookAndFeelTest::testWhiteBox);
        runOnEDT(SynthLookAndFeelTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.plaf.synth.SynthLookAndFeel");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.plaf.basic.BasicLookAndFeel"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() throws Exception {
        SynthLookAndFeel laf = new SynthLookAndFeel();
        check("Synth look and feel".equals(laf.getName()), "getName");
        check("Synth".equals(laf.getID()), "getID");
        check("Synth look and feel".equals(laf.getDescription()), "getDescription");
        check(laf.isNativeLookAndFeel() == false, "isNativeLookAndFeel false");
        check(laf.isSupportedLookAndFeel() == true, "isSupportedLookAndFeel true");

        UIDefaults defs = laf.getDefaults();
        check(defs != null && defs.size() > 0, "getDefaults non-empty");

        JButton b = new JButton();
        ComponentUI ui = SynthLookAndFeel.createUI(b);
        check(ui != null && ui instanceof SynthButtonUI, "createUI button");
        check(SynthLookAndFeel.getRegion(b) == Region.BUTTON, "getRegion button");

        SynthStyleFactory old = SynthLookAndFeel.getStyleFactory();
        SynthStyleFactory custom = new SynthStyleFactory() {
            public SynthStyle getStyle(JComponent c, Region r) { return null; }
        };
        SynthLookAndFeel.setStyleFactory(custom);
        check(SynthLookAndFeel.getStyleFactory() == custom, "setStyleFactory roundtrip");
        SynthLookAndFeel.setStyleFactory(old);
        check(SynthLookAndFeel.getStyleFactory() == old, "style factory restored");
    }
}
