/*
 * @test
 * @summary jdk.jshell.ExpressionToTypeInfo.ExpressionInfo conformance: expression info reflection.
 * @run main ExpressionInfoTest
 */

import java.lang.reflect.*;

public class ExpressionInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.ExpressionToTypeInfo$ExpressionInfo");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getConstructor() != null, "public no-arg ctor");
        check(c.getDeclaredFields().length > 0, "has fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
