/*
 * @test
 * @summary javax.swing.text.ZoneView conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main ZoneViewTest
 */

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ZoneView;

public class ZoneViewTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.ZoneView");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.text.BoxView"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        JFrame frame = new JFrame();
        JTextArea ta = new JTextArea("hello world");
        frame.add(ta);
        frame.pack();
        try {
            Element root = ta.getDocument().getDefaultRootElement();
            ZoneView zv = new ZoneView(root, View.Y_AXIS);
            check(zv.getAxis() == View.Y_AXIS, "axis stored from constructor");
            check(zv.getStartOffset() == 0 && zv.getEndOffset() == root.getEndOffset(), "offsets from element");
            check(zv.getDocument() == ta.getDocument(), "document from element");

            // zone-size properties
            check(zv.getMaximumZoneSize() == 8 * 1024, "default max zone size");
            zv.setMaximumZoneSize(1024);
            check(zv.getMaximumZoneSize() == 1024, "setMaximumZoneSize round-trip");

            // zones-loaded properties
            check(zv.getMaxZonesLoaded() == 3, "default max zones loaded");
            zv.setMaxZonesLoaded(7);
            check(zv.getMaxZonesLoaded() == 7, "setMaxZonesLoaded round-trip");
            zv.setMaxZonesLoaded(1);
            check(zv.getMaxZonesLoaded() == 1, "minimum valid zones loaded");
            boolean threw = false;
            try { zv.setMaxZonesLoaded(0); }
            catch (IllegalArgumentException e) { threw = true; }
            check(threw, "setMaxZonesLoaded(0) -> IllegalArgumentException");

            // not loaded yet: no zones created
            check(zv.getViewCount() == 0, "no children before parenting");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            frame.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(ZoneViewTest::testWhiteBox);
        runOnEDT(ZoneViewTest::testBlackBox);
        System.out.println("PASS (" + passed + " assertions)");
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void runOnEDT(ThrowingRunnable r) throws Exception {
        try {
            EventQueue.invokeAndWait(() -> {
                try { r.run(); }
                catch (Exception e) { throw new RuntimeException(e); }
            });
        } catch (InvocationTargetException ite) {
            Throwable t = ite.getCause();
            if (t instanceof RuntimeException) throw (RuntimeException) t;
            if (t instanceof Error) throw (Error) t;
            throw new RuntimeException(t);
        }
    }
}
