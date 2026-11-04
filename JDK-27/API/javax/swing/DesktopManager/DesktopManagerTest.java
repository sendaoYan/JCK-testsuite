/*
 * @test
 * @summary javax.swing.DesktopManager conformance: white-box structure + black-box behavior.
 * @run main DesktopManagerTest
 */

import java.lang.reflect.Modifier;

import javax.swing.DefaultDesktopManager;
import javax.swing.DesktopManager;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;

public class DesktopManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DesktopManager");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // exercise the interface through its default implementation
        DesktopManager dm = new DefaultDesktopManager();
        JInternalFrame f = new JInternalFrame("f");
        dm.setBoundsForFrame(f, 1, 2, 30, 40);
        check(f.getX() == 1 && f.getY() == 2 && f.getWidth() == 30 && f.getHeight() == 40, "setBoundsForFrame");
        dm.openFrame(f);
        dm.closeFrame(f);
        dm.activateFrame(f);
        dm.deactivateFrame(f);
        dm.beginDraggingFrame((JComponent) f);
        dm.endDraggingFrame((JComponent) f);
        check(true, "lifecycle smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
