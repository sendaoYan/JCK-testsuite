/*
 * @test
 * @summary javax.swing.event.MenuKeyListener conformance: white-box structure + black-box behavior.
 * @run main MenuKeyListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.MenuKeyEvent;
import javax.swing.event.MenuKeyListener;
import java.util.HashSet;
import java.util.Set;

public class MenuKeyListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkMethod(Class<?> c, String name, Class<?>... params) {
        try {
            Method m = c.getMethod(name, params);
            check(m.getReturnType() == void.class, name + " returns void");
            check(Modifier.isAbstract(m.getModifiers()), name + " abstract");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("missing method " + name);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MenuKeyListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == true, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(c.getInterfaces()[0].getName().equals("java.util.EventListener"), "interface 0");
        checkMethod(c, "menuKeyTyped", MenuKeyEvent.class);
        checkMethod(c, "menuKeyPressed", MenuKeyEvent.class);
        checkMethod(c, "menuKeyReleased", MenuKeyEvent.class);
        check(c.getDeclaredMethods().length == 3, "declared method count == 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final MenuKeyEvent[] last = new MenuKeyEvent[1];
        MenuKeyListener l = new MenuKeyListener() {
            public void menuKeyTyped(MenuKeyEvent e) { fired[0] = "typed"; last[0] = e; }
            public void menuKeyPressed(MenuKeyEvent e) { fired[0] = "pressed"; last[0] = e; }
            public void menuKeyReleased(MenuKeyEvent e) { fired[0] = "released"; last[0] = e; }
        };

        Component src = new Container();
        JMenuItem item = new JMenuItem("item");
        MenuElement[] path = new MenuElement[] { item };
        MenuSelectionManager msm = new MenuSelectionManager();

        MenuKeyEvent typed = new MenuKeyEvent(src, KeyEvent.KEY_TYPED, 2000L, 0, KeyEvent.VK_UNDEFINED, 'a', path, msm);
        l.menuKeyTyped(typed);
        check("typed".equals(fired[0]), "menuKeyTyped fired");
        check(last[0].getSource() == src, "source round-trip");
        check(last[0].getPath() == path && last[0].getPath()[0] == item, "getPath round-trip");
        check(last[0].getMenuSelectionManager() == msm, "getMenuSelectionManager round-trip");
        check(last[0].getKeyChar() == 'a', "getKeyChar round-trip");
        check(last[0].getID() == KeyEvent.KEY_TYPED, "getID round-trip");

        MenuKeyEvent pressed = new MenuKeyEvent(src, KeyEvent.KEY_PRESSED, 2001L, 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED, path, msm);
        l.menuKeyPressed(pressed);
        check("pressed".equals(fired[0]) && last[0] == pressed, "menuKeyPressed fired");
        check(last[0].getKeyCode() == KeyEvent.VK_ENTER, "getKeyCode round-trip");

        MenuKeyEvent released = new MenuKeyEvent(src, KeyEvent.KEY_RELEASED, 2002L, 0, KeyEvent.VK_ENTER, KeyEvent.CHAR_UNDEFINED, path, msm);
        l.menuKeyReleased(released);
        check("released".equals(fired[0]) && last[0] == released, "menuKeyReleased fired");
        check(last[0].getID() == KeyEvent.KEY_RELEASED, "released getID round-trip");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MenuKeyListener", false, MenuKeyListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("menuKeyTyped"), "method menuKeyTyped");
        check(names.contains("menuKeyPressed"), "method menuKeyPressed");
        check(names.contains("menuKeyReleased"), "method menuKeyReleased");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
