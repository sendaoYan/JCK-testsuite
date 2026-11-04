/*
 * @test
 * @summary javax.swing.event.MenuDragMouseEvent conformance: white-box structure + black-box behavior.
 * @run main MenuDragMouseEventTest
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.lang.reflect.Modifier;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class MenuDragMouseEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.MenuDragMouseEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.MouseEvent"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        Component source = new Container();
        JMenuItem item = new JMenuItem("item");
        MenuElement[] path = new MenuElement[] { item };
        MenuSelectionManager manager = new MenuSelectionManager();

        javax.swing.event.MenuDragMouseEvent ev = new javax.swing.event.MenuDragMouseEvent(
            source, MouseEvent.MOUSE_DRAGGED, 12345L, 0, 5, 6, 1, false, path, manager);

        check(ev.getSource() == source, "getSource round-trip");
        check(ev.getID() == MouseEvent.MOUSE_DRAGGED, "getID MOUSE_DRAGGED");
        check(ev.getWhen() == 12345L, "getWhen round-trip");
        check(ev.getX() == 5 && ev.getY() == 6, "getX/getY round-trip");
        check(ev.getClickCount() == 1, "getClickCount round-trip");
        check(ev.isPopupTrigger() == false, "isPopupTrigger false");
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
        Class<?> c = Class.forName("javax.swing.event.MenuDragMouseEvent", false, MenuDragMouseEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.awt.event.MouseEvent"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPath"), "method getPath");
        check(names.contains("getMenuSelectionManager"), "method getMenuSelectionManager");
    }


    // ---- Part 2b: 9-arg constructor (deprecated modifiers form) ----
    static void testExtraContracts() {
        Component source9 = new Container();
        JMenuItem item9 = new JMenuItem("item9");
        MenuElement[] path9 = new MenuElement[] { item9 };
        MenuSelectionManager manager9 = new MenuSelectionManager();

        javax.swing.event.MenuDragMouseEvent ev9 = new javax.swing.event.MenuDragMouseEvent(
            source9, MouseEvent.MOUSE_ENTERED, 999L, 8, 3, 4, 2, true, path9, manager9);
        check(ev9.getSource() == source9, "9-arg source round-trip");
        check(ev9.getID() == MouseEvent.MOUSE_ENTERED, "9-arg getID MOUSE_ENTERED");
        check(ev9.getWhen() == 999L, "9-arg getWhen round-trip");
        check(ev9.getX() == 3 && ev9.getY() == 4, "9-arg coordinates");
        check(ev9.getClickCount() == 2, "9-arg click count");
        check(ev9.isPopupTrigger(), "9-arg popup trigger");
        check(ev9.getPath() == path9, "9-arg path identity");
        check(ev9.getMenuSelectionManager() == manager9, "9-arg manager identity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        testExtraContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
