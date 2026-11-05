/*
 * @test
 * @summary jdk.jshell.TypePrinter.AnonymousTypeKind conformance: anonymous type kind enum.
 * @run main AnonymousTypeKindTest
 */

import java.lang.reflect.*;

public class AnonymousTypeKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: enum table ----
    static void testTable() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.TypePrinter$AnonymousTypeKind");
        Object[] consts = c.getEnumConstants();
        check(consts.length == 3, "3 constants");
        // documented order: SUPER (superclass position), DECLARE (declared), DISPLAY (displayed)
        check(consts[0].toString().equals("SUPER"), "order[0] SUPER");
        check(consts[1].toString().equals("DECLARE"), "order[1] DECLARE");
        check(consts[2].toString().equals("DISPLAY"), "order[2] DISPLAY");
        check(c.getMethod("valueOf", String.class).invoke(null, "DISPLAY") == consts[2], "valueOf");
        try { c.getMethod("valueOf", String.class).invoke(null, "BOGUS"); check(false, "valueOf bogus IAE"); }
        catch (java.lang.reflect.InvocationTargetException expected) { check(true, "valueOf bogus IAE"); }
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.TypePrinter$AnonymousTypeKind");
        check(c.isEnum(), "is enum");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getEnumConstants().length == 3, "3 constants");
        testTable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
