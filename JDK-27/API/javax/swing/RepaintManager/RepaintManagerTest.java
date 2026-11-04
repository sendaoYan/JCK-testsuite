/*
 * @test
 * @summary javax.swing.RepaintManager conformance: white-box structure + black-box behavior.
 * @run main RepaintManagerTest
 */

import java.awt.Dimension;
import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.RepaintManager;

public class RepaintManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.RepaintManager");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RepaintManager rm = RepaintManager.currentManager(new JPanel());
        check(rm != null, "currentManager");
        check(RepaintManager.currentManager(new JPanel()) == rm, "currentManager singleton");

        check(rm.isDoubleBufferingEnabled(), "double buffering default");
        rm.setDoubleBufferingEnabled(false);
        check(!rm.isDoubleBufferingEnabled(), "double buffering disabled");
        rm.setDoubleBufferingEnabled(true);

        // Default is Integer.MAX_VALUE in headless mode, or the largest screen
        // size when a display is present — both are positive, never zero/negative.
        Dimension d = rm.getDoubleBufferMaximumSize();
        check(d != null && d.width > 0 && d.height > 0,
                "default double buffer max size positive");
        rm.setDoubleBufferMaximumSize(new Dimension(100, 100));
        check(rm.getDoubleBufferMaximumSize().equals(new Dimension(100, 100)),
                "set double buffer max size");

        RepaintManager.setCurrentManager(rm);
        check(RepaintManager.currentManager(new JPanel()) == rm, "setCurrentManager");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
