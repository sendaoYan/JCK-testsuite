/*
 * @test
 * @summary javax.swing.event.PopupMenuListener conformance: white-box structure + black-box behavior.
 * @run main PopupMenuListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public class PopupMenuListenerTest {
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
        Class<?> c = Class.forName("javax.swing.event.PopupMenuListener");
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
        checkMethod(c, "popupMenuWillBecomeVisible", PopupMenuEvent.class);
        checkMethod(c, "popupMenuWillBecomeInvisible", PopupMenuEvent.class);
        checkMethod(c, "popupMenuCanceled", PopupMenuEvent.class);
        check(c.getDeclaredMethods().length == 3, "declared method count == 3");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final PopupMenuEvent[] last = new PopupMenuEvent[1];
        PopupMenuListener l = new PopupMenuListener() {
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) { fired[0] = "visible"; last[0] = e; }
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) { fired[0] = "invisible"; last[0] = e; }
            public void popupMenuCanceled(PopupMenuEvent e) { fired[0] = "canceled"; last[0] = e; }
        };

        Object src = new Object();
        PopupMenuEvent visible = new PopupMenuEvent(src);
        l.popupMenuWillBecomeVisible(visible);
        check("visible".equals(fired[0]) && last[0] == visible, "popupMenuWillBecomeVisible fired");
        check(last[0].getSource() == src, "source round-trip");

        PopupMenuEvent invisible = new PopupMenuEvent(src);
        l.popupMenuWillBecomeInvisible(invisible);
        check("invisible".equals(fired[0]) && last[0] == invisible, "popupMenuWillBecomeInvisible fired");

        PopupMenuEvent canceled = new PopupMenuEvent(src);
        l.popupMenuCanceled(canceled);
        check("canceled".equals(fired[0]) && last[0] == canceled, "popupMenuCanceled fired");
        check(last[0].getSource() == src, "canceled source round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
