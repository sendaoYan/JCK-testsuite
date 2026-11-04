/*
 * @test
 * @summary jdk.jshell.TypePrinter.AnonymousTypeKind conformance: anonymous type kind enum.
 * @run main AnonymousTypeKindTest
 */

import java.lang.reflect.*;

public class AnonymousTypeKindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.TypePrinter$AnonymousTypeKind");
        check(c.isEnum(), "is enum");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getEnumConstants().length == 3, "3 constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
