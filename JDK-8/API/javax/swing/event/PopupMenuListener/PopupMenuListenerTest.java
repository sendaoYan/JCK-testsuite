/*
 * @test
 * @summary javax.swing.event.PopupMenuListener conformance: white-box structure + black-box behavior.
 * @run main PopupMenuListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.util.HashSet;
import java.util.Set;

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


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.PopupMenuListener", false, PopupMenuListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("popupMenuWillBecomeVisible"), "method popupMenuWillBecomeVisible");
        check(names.contains("popupMenuWillBecomeInvisible"), "method popupMenuWillBecomeInvisible");
        check(names.contains("popupMenuCanceled"), "method popupMenuCanceled");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
