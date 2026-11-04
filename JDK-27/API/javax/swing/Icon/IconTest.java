/*
 * @test
 * @summary javax.swing.Icon conformance: white-box structure + black-box behavior.
 * @run main IconTest
 */

import java.awt.Component;
import java.awt.Graphics;
import java.lang.reflect.Modifier;

import javax.swing.Icon;

public class IconTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.Icon");
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
        final boolean[] painted = {false};
        Icon icon = new Icon() {
            public void paintIcon(Component c, Graphics g, int x, int y) { painted[0] = true; }
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 8; }
        };

        check(icon.getIconWidth() == 16, "getIconWidth");
        check(icon.getIconHeight() == 8, "getIconHeight");
        icon.paintIcon(null, null, 0, 0);
        check(painted[0], "paintIcon invoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
