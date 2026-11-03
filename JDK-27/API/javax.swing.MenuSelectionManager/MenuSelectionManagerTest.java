/*
 * @test
 * @summary javax.swing.MenuSelectionManager conformance: white-box structure + black-box behavior.
 * @run main MenuSelectionManagerTest
 */

import java.lang.reflect.Modifier;

import javax.swing.JPanel;
import javax.swing.MenuSelectionManager;
import javax.swing.event.ChangeListener;

public class MenuSelectionManagerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.MenuSelectionManager");
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
        MenuSelectionManager m = MenuSelectionManager.defaultManager();
        check(m != null, "defaultManager");
        check(MenuSelectionManager.defaultManager() == m, "defaultManager singleton");
        check(m.getSelectedPath().length == 0, "initial empty path");
        m.setSelectedPath(null);
        check(m.getSelectedPath().length == 0, "path after null");
        m.clearSelectedPath();
        check(m.getSelectedPath().length == 0, "path after clear");
        check(!m.isComponentPartOfCurrentMenu(new JPanel()), "not part of menu");

        ChangeListener l = e -> { };
        m.addChangeListener(l);
        m.removeChangeListener(l);
        check(m.getChangeListeners().length == 0, "listener add/remove");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
