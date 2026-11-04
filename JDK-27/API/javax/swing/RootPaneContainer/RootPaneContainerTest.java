/*
 * @test
 * @summary javax.swing.RootPaneContainer conformance: white-box structure + black-box behavior.
 * @run main RootPaneContainerTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;

public class RootPaneContainerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.RootPaneContainer");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(!c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        RootPaneContainer rpc = new JInternalFrame();
        check(rpc.getContentPane() != null, "getContentPane");
        check(rpc.getRootPane() != null, "getRootPane");
        check(rpc.getLayeredPane() != null, "getLayeredPane");
        check(rpc.getGlassPane() != null, "getGlassPane");

        JPanel content = new JPanel();
        rpc.setContentPane(content);
        check(rpc.getContentPane() == content, "setContentPane");
        JPanel glass = new JPanel();
        rpc.setGlassPane(glass);
        check(rpc.getGlassPane() == glass, "setGlassPane");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
