/*
 * @test
 * @summary java.security.KeyRep.Type conformance: white-box structure + black-box behavior.
 * @run main TypeTest
 */

import java.lang.reflect.Modifier;
import java.security.KeyRep;

public class TypeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyRep$Type");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() == Enum.class, "extends Enum");
        checkEnumConstants(c, new String[]{"SECRET", "PUBLIC", "PRIVATE"});
        checkMethods(c, new String[]{"valueOf", "values"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyRep.Type[] values = KeyRep.Type.values();
        check(values.length == 3, "values length 3");

        check(KeyRep.Type.valueOf("SECRET") == KeyRep.Type.SECRET, "valueOf SECRET");
        check(KeyRep.Type.valueOf("PUBLIC") == KeyRep.Type.PUBLIC, "valueOf PUBLIC");
        check(KeyRep.Type.valueOf("PRIVATE") == KeyRep.Type.PRIVATE, "valueOf PRIVATE");

        // name() round-trip
        for (KeyRep.Type t : values) {
            check(KeyRep.Type.valueOf(t.name()) == t, "name/valueOf roundtrip " + t.name());
        }

        // ordinal ordering stable (declaration order SECRET, PUBLIC, PRIVATE)
        check(KeyRep.Type.SECRET.ordinal() == 0, "SECRET ordinal 0");
        check(KeyRep.Type.PUBLIC.ordinal() == 1, "PUBLIC ordinal 1");
        check(KeyRep.Type.PRIVATE.ordinal() == 2, "PRIVATE ordinal 2");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
