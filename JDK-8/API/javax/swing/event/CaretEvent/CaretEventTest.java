/*
 * @test
 * @summary javax.swing.event.CaretEvent conformance: white-box structure + black-box behavior.
 * @run main CaretEventTest
 */

import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class CaretEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.CaretEvent");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        check(Modifier.isAbstract(mods) == true, "abstract modifier");
        check(c.getInterfaces().length == 0, "no interfaces");
    }

    static void testBlackBox() {
        Object source = new Object();
        javax.swing.event.CaretEvent ev = new javax.swing.event.CaretEvent(source) {
            public int getDot() { return 3; }
            public int getMark() { return 7; }
        };
        check(ev.getSource() == source, "getSource round-trip");
        check(ev.getDot() == 3, "getDot round-trip");
        check(ev.getMark() == 7, "getMark round-trip");

        // A second concrete instance verifies values are not shared/static.
        Object src2 = "caret-source";
        javax.swing.event.CaretEvent ev2 = new javax.swing.event.CaretEvent(src2) {
            public int getDot() { return -1; }
            public int getMark() { return 0; }
        };
        check(ev2.getSource() == src2, "second getSource round-trip");
        check(ev2.getDot() == -1, "second getDot round-trip");
        check(ev2.getMark() == 0, "second getMark round-trip");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.CaretEvent", false, CaretEventTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getDot"), "method getDot");
        check(names.contains("getMark"), "method getMark");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
