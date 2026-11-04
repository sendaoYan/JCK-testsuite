/*
 * @test
 * @summary javax.swing.event.MenuKeyEvent conformance: white-box structure + black-box behavior.
 * @run main MenuKeyEventTest
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.lang.reflect.Modifier;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class MenuKeyEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MenuKeyEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.KeyEvent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        Component source = new Container();
        JMenuItem item = new JMenuItem("item");
        MenuElement[] path = new MenuElement[] { item };
        MenuSelectionManager manager = new MenuSelectionManager();

        javax.swing.event.MenuKeyEvent ev = new javax.swing.event.MenuKeyEvent(
            source, KeyEvent.KEY_PRESSED, 67890L, 0, KeyEvent.VK_A, 'a', path, manager);

        check(ev.getSource() == source, "getSource round-trip");
        check(ev.getID() == KeyEvent.KEY_PRESSED, "getID KEY_PRESSED");
        check(ev.getWhen() == 67890L, "getWhen round-trip");
        check(ev.getKeyCode() == KeyEvent.VK_A, "getKeyCode round-trip");
        check(ev.getKeyChar() == 'a', "getKeyChar round-trip");
        check(ev.getPath() == path, "getPath identity round-trip");
        check(ev.getPath().length == 1 && ev.getPath()[0] == item, "getPath contents");
        check(ev.getMenuSelectionManager() == manager, "getMenuSelectionManager round-trip");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MenuKeyEvent", false, MenuKeyEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.KeyEvent"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPath"), "method getPath");
        check(names.contains("getMenuSelectionManager"), "method getMenuSelectionManager");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
