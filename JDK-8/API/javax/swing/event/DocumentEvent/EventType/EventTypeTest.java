/*
 * @test
 * @summary javax.swing.event.DocumentEvent.EventType conformance: white-box structure + black-box behavior.
 * @run main EventTypeTest
 */

import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class EventTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.DocumentEvent$EventType");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(Modifier.isAbstract(mods) == false, "abstract modifier");
        check(Modifier.isFinal(mods) == true, "final modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        javax.swing.event.DocumentEvent.EventType insert = javax.swing.event.DocumentEvent.EventType.INSERT;
        javax.swing.event.DocumentEvent.EventType remove = javax.swing.event.DocumentEvent.EventType.REMOVE;
        javax.swing.event.DocumentEvent.EventType change = javax.swing.event.DocumentEvent.EventType.CHANGE;

        check(insert != null, "INSERT non-null");
        check(remove != null, "REMOVE non-null");
        check(change != null, "CHANGE non-null");
        check(insert.toString().equals("INSERT"), "INSERT toString");
        check(remove.toString().equals("REMOVE"), "REMOVE toString");
        check(change.toString().equals("CHANGE"), "CHANGE toString");
        check(insert != remove, "INSERT != REMOVE");
        check(remove != change, "REMOVE != CHANGE");
        check(change != insert, "CHANGE != INSERT");
        check(javax.swing.event.DocumentEvent.EventType.INSERT == insert, "static INSERT identity stable");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.DocumentEvent$EventType", false, EventTypeTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("toString"), "method toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
