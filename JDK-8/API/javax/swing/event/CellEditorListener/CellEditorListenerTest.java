/*
 * @test
 * @summary javax.swing.event.CellEditorListener conformance: white-box structure + black-box behavior.
 * @run main CellEditorListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.util.HashSet;
import java.util.Set;

public class CellEditorListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.CellEditorListener");
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
        checkMethod(c, "editingStopped", ChangeEvent.class);
        checkMethod(c, "editingCanceled", ChangeEvent.class);
        check(c.getDeclaredMethods().length == 2, "declared method count == 2");
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

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        final String[] fired = new String[1];
        final ChangeEvent[] last = new ChangeEvent[1];
        CellEditorListener l = new CellEditorListener() {
            public void editingStopped(ChangeEvent e) { fired[0] = "stopped"; last[0] = e; }
            public void editingCanceled(ChangeEvent e) { fired[0] = "canceled"; last[0] = e; }
        };

        Object src = new Object();
        ChangeEvent stopped = new ChangeEvent(src);
        l.editingStopped(stopped);
        check("stopped".equals(fired[0]), "editingStopped fired");
        check(last[0] == stopped, "editingStopped event instance");
        check(last[0].getSource() == src, "editingStopped source round-trip");

        ChangeEvent canceled = new ChangeEvent(src);
        l.editingCanceled(canceled);
        check("canceled".equals(fired[0]), "editingCanceled fired");
        check(last[0] == canceled, "editingCanceled event instance");
        check(last[0].getSource() == src, "editingCanceled source round-trip");

        check(stopped.getSource() != canceled, "distinct event instances");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.CellEditorListener", false, CellEditorListenerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface(), "is interface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(hasInterface(c, "java.util.EventListener"), "implements java.util.EventListener");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("editingStopped"), "method editingStopped");
        check(names.contains("editingCanceled"), "method editingCanceled");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
