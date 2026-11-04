/*
 * @test
 * @summary javax.swing.event.RowSorterEvent.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.RowSorterEvent$Type");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == true, "is enum");
        check(Modifier.isPublic(mods) == true, "public modifier");
        check(Modifier.isProtected(mods) == false, "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 2, "enum constant count");
        check(((Enum<?>) ec[0]).name().equals("SORT_ORDER_CHANGED"), "enum constant 0");
        check(((Enum<?>) ec[1]).name().equals("SORTED"), "enum constant 1");
    }

    static void testBlackBox() {
        javax.swing.event.RowSorterEvent.Type orderChanged = javax.swing.event.RowSorterEvent.Type.SORT_ORDER_CHANGED;
        javax.swing.event.RowSorterEvent.Type sorted = javax.swing.event.RowSorterEvent.Type.SORTED;

        check(orderChanged != null, "SORT_ORDER_CHANGED non-null");
        check(sorted != null, "SORTED non-null");
        check(orderChanged.name().equals("SORT_ORDER_CHANGED"), "name SORT_ORDER_CHANGED");
        check(sorted.name().equals("SORTED"), "name SORTED");
        check(orderChanged.ordinal() == 0, "SORT_ORDER_CHANGED ordinal 0");
        check(sorted.ordinal() == 1, "SORTED ordinal 1");
        check(orderChanged != sorted, "constants distinct");

        javax.swing.event.RowSorterEvent.Type[] all = javax.swing.event.RowSorterEvent.Type.values();
        check(all.length == 2, "values length 2");
        check(all[0] == orderChanged && all[1] == sorted, "values order");

        check(javax.swing.event.RowSorterEvent.Type.valueOf("SORTED") == sorted, "valueOf SORTED");
        check(javax.swing.event.RowSorterEvent.Type.valueOf("SORT_ORDER_CHANGED") == orderChanged, "valueOf SORT_ORDER_CHANGED");
    }


    // ---- Part 3: legacy shallow coverage (retained; assertions preserved verbatim) ----
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    // Legacy shallow assertions preserved verbatim from the original JDK-8 test.
    static void testLegacyShallow() throws Exception {
        Class<?> c = Class.forName("javax.swing.event.RowSorterEvent$Type", false, TypeTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        Object[] en = c.getEnumConstants();
        check(en != null && en.length == 2, "enum constant count");
        check(en != null && en.length > 0 && "SORT_ORDER_CHANGED".equals(((Enum) en[0]).name()), "const SORT_ORDER_CHANGED");
        check(en != null && en.length > 1 && "SORTED".equals(((Enum) en[1]).name()), "const SORTED");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("values"), "method values");
        check(names.contains("valueOf"), "method valueOf");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testLegacyShallow();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
