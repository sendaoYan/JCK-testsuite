/*
 * @test
 * @summary javax.swing.SortOrder conformance: white-box structure + black-box behavior.
 * @run main SortOrderTest
 */

import java.lang.reflect.Modifier;

import javax.swing.SortOrder;

public class SortOrderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.swing.SortOrder");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "is interface");
        check(!c.isAnnotation(), "is annotation");
        check(c.isEnum(), "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(!Modifier.isProtected(mods), "protected modifier");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no interfaces");
        Object[] ec = c.getEnumConstants();
        check(ec != null && ec.length == 3, "enum constant count");
        check(((Enum<?>) ec[0]).name().equals("ASCENDING"), "enum constant 0");
        check(((Enum<?>) ec[1]).name().equals("DESCENDING"), "enum constant 1");
        check(((Enum<?>) ec[2]).name().equals("UNSORTED"), "enum constant 2");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        check(SortOrder.values().length == 3, "values length");
        check(SortOrder.valueOf("ASCENDING") == SortOrder.ASCENDING, "valueOf ASCENDING");
        check(SortOrder.valueOf("DESCENDING") == SortOrder.DESCENDING, "valueOf DESCENDING");
        check(SortOrder.valueOf("UNSORTED") == SortOrder.UNSORTED, "valueOf UNSORTED");
        check(SortOrder.ASCENDING.ordinal() == 0, "ordinal ascending");
        check(SortOrder.DESCENDING.ordinal() == 1, "ordinal descending");
        check(SortOrder.UNSORTED.ordinal() == 2, "ordinal unsorted");
        check("ASCENDING".equals(SortOrder.ASCENDING.name()), "name");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
