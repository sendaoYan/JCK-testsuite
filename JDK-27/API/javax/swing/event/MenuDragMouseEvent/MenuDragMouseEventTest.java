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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
