/*
 * @test
 * @summary javax.swing.event.MenuDragMouseListener conformance: white-box structure + black-box behavior.
 * @run main MenuDragMouseListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.event.MenuDragMouseEvent;
import javax.swing.event.MenuDragMouseListener;
import java.util.HashSet;
import java.util.Set;

public class MenuDragMouseListenerTest {
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
        Class<?> c = Class.forName("javax.swing.event.MenuDragMouseListener");
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
        checkMethod(c, "menuDragMouseEntered", MenuDragMouseEvent.class);
        checkMethod(c, "menuDragMouseExited", MenuDragMouseEvent.class);
        checkMethod(c, "menuDragMouseDragged", MenuDragMouseEvent.class);
        checkMethod(c, "menuDragMouseReleased", MenuDragMouseEvent.class);
        check(c.getDeclaredMethods().length == 4, "declared method count == 4");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final MenuDragMouseEvent[] last = new MenuDragMouseEvent[1];
        MenuDragMouseListener l = new MenuDragMouseListener() {
            public void menuDragMouseEntered(MenuDragMouseEvent e) { fired[0] = "entered"; last[0] = e; }
            public void menuDragMouseExited(MenuDragMouseEvent e) { fired[0] = "exited"; last[0] = e; }
            public void menuDragMouseDragged(MenuDragMouseEvent e) { fired[0] = "dragged"; last[0] = e; }
            public void menuDragMouseReleased(MenuDragMouseEvent e) { fired[0] = "released"; last[0] = e; }
        };

        Component src = new Container();
        JMenuItem item = new JMenuItem("item");
        MenuElement[] path = new MenuElement[] { item };
        MenuSelectionManager msm = new MenuSelectionManager();

        MenuDragMouseEvent entered = new MenuDragMouseEvent(src, MouseEvent.MOUSE_ENTERED, 1000L, 0, 10, 20, 1, false, path, msm);
        l.menuDragMouseEntered(entered);
        check("entered".equals(fired[0]), "menuDragMouseEntered fired");
        check(last[0].getSource() == src, "source round-trip");
        check(last[0].getPath() == path && last[0].getPath()[0] == item, "getPath round-trip");
        check(last[0].getMenuSelectionManager() == msm, "getMenuSelectionManager round-trip");
        check(last[0].getX() == 10 && last[0].getY() == 20, "x/y round-trip");
        check(last[0].getClickCount() == 1, "clickCount round-trip");
        check(last[0].isPopupTrigger() == false, "isPopupTrigger round-trip");
        check(last[0].getID() == MouseEvent.MOUSE_ENTERED, "getID round-trip");

        MenuDragMouseEvent exited = new MenuDragMouseEvent(src, MouseEvent.MOUSE_EXITED, 1001L, 0, 11, 21, 1, false, path, msm);
        l.menuDragMouseExited(exited);
        check("exited".equals(fired[0]) && last[0] == exited, "menuDragMouseExited fired");

        MenuDragMouseEvent dragged = new MenuDragMouseEvent(src, MouseEvent.MOUSE_DRAGGED, 1002L, 0, 12, 22, 1, false, path, msm);
        l.menuDragMouseDragged(dragged);
        check("dragged".equals(fired[0]) && last[0] == dragged, "menuDragMouseDragged fired");

        MenuDragMouseEvent released = new MenuDragMouseEvent(src, MouseEvent.MOUSE_RELEASED, 1003L, 0, 13, 23, 1, true, path, msm);
        l.menuDragMouseReleased(released);
        check("released".equals(fired[0]) && last[0] == released, "menuDragMouseReleased fired");
        check(last[0].isPopupTrigger() == true, "released popupTrigger round-trip");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MenuDragMouseListener", false, MenuDragMouseListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("menuDragMouseEntered"), "method menuDragMouseEntered");
        check(names.contains("menuDragMouseExited"), "method menuDragMouseExited");
        check(names.contains("menuDragMouseDragged"), "method menuDragMouseDragged");
        check(names.contains("menuDragMouseReleased"), "method menuDragMouseReleased");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
