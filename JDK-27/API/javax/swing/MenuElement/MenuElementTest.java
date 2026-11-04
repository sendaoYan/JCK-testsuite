/*
 * @test
 * @summary javax.swing.MenuElement conformance: white-box structure + black-box behavior.
 * @run main MenuElementTest
 */

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

public class MenuElementTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.MenuElement");
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
        final Component comp = new JPanel();
        MenuElement me = new MenuElement() {
            public void processMouseEvent(MouseEvent e, MenuElement[] path, MenuSelectionManager m) { }
            public void processKeyEvent(KeyEvent e, MenuElement[] path, MenuSelectionManager m) { }
            public void menuSelectionChanged(boolean isIncluded) { }
            public MenuElement[] getSubElements() { return new MenuElement[0]; }
            public Component getComponent() { return comp; }
        };
        check(me.getComponent() == comp, "getComponent");
        check(me.getSubElements().length == 0, "empty subelements");
        me.menuSelectionChanged(true); // must not throw
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
