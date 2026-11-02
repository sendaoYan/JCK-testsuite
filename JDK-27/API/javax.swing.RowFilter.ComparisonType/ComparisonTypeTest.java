/*
 * @test
 * @summary javax.swing.RowFilter.ComparisonType conformance: type shape and supertypes.
 * @run main ComparisonTypeTest
 */

import java.lang.reflect.Modifier;

public class ComparisonTypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.swing.RowFilter$ComparisonType");
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
        check(ec != null && ec.length == 4, "enum constant count");
        check(((Enum<?>) ec[0]).name().equals("BEFORE"), "enum constant 0");
        check(((Enum<?>) ec[1]).name().equals("AFTER"), "enum constant 1");
        check(((Enum<?>) ec[2]).name().equals("EQUAL"), "enum constant 2");
        check(((Enum<?>) ec[3]).name().equals("NOT_EQUAL"), "enum constant 3");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
