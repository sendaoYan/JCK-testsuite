/*
 * @test
 * @summary javax.swing.text.JTextComponent.DropLocation conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main/othervm --add-opens=java.desktop/javax.swing.text=ALL-UNNAMED DropLocationTest
 */

import java.awt.EventQueue;
import java.awt.Point;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import javax.swing.DropMode;
import javax.swing.JTextArea;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;

public class DropLocationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean throwsIAE(ThrowingRunnable r) {
        try { r.run(); return false; }
        catch (InvocationTargetException e) { return e.getCause() instanceof IllegalArgumentException; }
        catch (Throwable t) { return t instanceof IllegalArgumentException; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.JTextComponent$DropLocation");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.swing.TransferHandler$DropLocation"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(Modifier.isFinal(mods) == true, "final modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
        check(c.getMethod("getIndex") != null, "getIndex present");
        check(c.getMethod("getBias") != null, "getBias present");
        check(c.getMethod("getDropPoint") != null, "getDropPoint present");
        Constructor<?> ctor = c.getDeclaredConstructor(Point.class, int.class, Position.Bias.class);
        check(Modifier.isPrivate(ctor.getModifiers()), "ctor is private");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Position.Bias exposes exactly two constants used by DropLocation.getBias()
        check(Position.Bias.Forward != null && Position.Bias.Backward != null, "Bias.Forward/Backward present");
        check(Position.Bias.Forward != Position.Bias.Backward, "Bias constants distinct");
        check("Forward".equals(Position.Bias.Forward.toString()), "Bias.Forward toString");
        check("Backward".equals(Position.Bias.Backward.toString()), "Bias.Backward toString");

        // The only constructor is private (DnD system is the factory); reach it via reflection.
        Constructor<JTextComponent.DropLocation> ctor =
            JTextComponent.DropLocation.class.getDeclaredConstructor(Point.class, int.class, Position.Bias.class);
        ctor.setAccessible(true);
        JTextComponent.DropLocation dl = ctor.newInstance(new Point(5, 7), 3, Position.Bias.Backward);

        check(dl.getIndex() == 3, "getIndex round trip");
        check(dl.getBias() == Position.Bias.Backward, "getBias round trip");
        check(dl.getDropPoint().equals(new Point(5, 7)), "getDropPoint round trip");
        check(dl.getDropPoint() != new Point(5, 7), "getDropPoint returns a copy");
        String s = dl.toString();
        check(s.contains("javax.swing.text.JTextComponent$DropLocation") && s.contains("index=3") && s.contains("bias=Backward"),
              "toString contains class/index/bias");

        JTextComponent.DropLocation fwd = ctor.newInstance(new Point(0, 0), 0, Position.Bias.Forward);
        check(fwd.getBias() == Position.Bias.Forward && fwd.getIndex() == 0, "Forward bias instance");
        check(throwsIAE(() -> ctor.newInstance(null, 0, Position.Bias.Forward)), "null point -> IAE");

        // Public JTextComponent API feeding DropLocation: default drop location null + drop mode round trip
        JTextArea area = new JTextArea("text");
        check(area.getDropLocation() == null, "getDropLocation null before DnD");
        check(area.getDropMode() == DropMode.USE_SELECTION, "default drop mode USE_SELECTION");
        area.setDropMode(DropMode.INSERT);
        check(area.getDropMode() == DropMode.INSERT, "setDropMode(INSERT) round trip");
        check(throwsIAE(() -> area.setDropMode(null)), "setDropMode(null) -> IAE");
        check(throwsIAE(() -> area.setDropMode(DropMode.ON)), "setDropMode(ON) -> IAE");
    }

    public static void main(String[] args) throws Exception {
        runOnEDT(DropLocationTest::testWhiteBox);
        runOnEDT(DropLocationTest::testBlackBox);
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
