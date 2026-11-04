/*
 * @test
 * @summary javax.swing.DefaultDesktopManager conformance: white-box structure + black-box behavior.
 * @run main DefaultDesktopManagerTest
 */

import java.awt.Rectangle;
import java.lang.reflect.Modifier;

import javax.swing.DefaultDesktopManager;
import javax.swing.JInternalFrame;

public class DefaultDesktopManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.DefaultDesktopManager");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(!Modifier.isAbstract(mods), "abstract modifier");
        check(c.getInterfaces().length == 2, "interface count");
        check(c.getInterfaces()[0].getName().equals("javax.swing.DesktopManager"), "interface 0");
        check(c.getInterfaces()[1].getName().equals("java.io.Serializable"), "interface 1");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        DefaultDesktopManager dm = new DefaultDesktopManager();
        JInternalFrame f = new JInternalFrame("frame");

        // bounds operations are directly observable
        dm.setBoundsForFrame(f, 10, 20, 100, 80);
        check(new Rectangle(10, 20, 100, 80).equals(f.getBounds()), "setBoundsForFrame");

        dm.dragFrame(f, 5, 6);
        check(f.getX() == 5 && f.getY() == 6, "dragFrame");

        dm.resizeFrame(f, 0, 0, 200, 100);
        check(new Rectangle(0, 0, 200, 100).equals(f.getBounds()), "resizeFrame");

        // lifecycle methods are safe no-ops when the frame has no desktop
        dm.openFrame(f);
        dm.closeFrame(f);
        dm.activateFrame(f);
        dm.deactivateFrame(f);
        dm.maximizeFrame(f);
        dm.minimizeFrame(f);
        dm.iconifyFrame(f);
        dm.deiconifyFrame(f);
        dm.beginDraggingFrame(f);
        dm.endDraggingFrame(f);
        dm.beginResizingFrame(f, 0);
        dm.endResizingFrame(f);
        check(true, "lifecycle smoke");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
