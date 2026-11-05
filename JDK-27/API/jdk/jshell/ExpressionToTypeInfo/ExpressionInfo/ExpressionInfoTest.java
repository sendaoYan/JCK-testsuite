/*
 * @test
 * @summary jdk.jshell.ExpressionToTypeInfo.ExpressionInfo conformance: expression info reflection.
 * @run main/othervm --add-opens=jdk.jshell/jdk.jshell=ALL-UNNAMED ExpressionInfoTest
 */

import java.lang.reflect.*;

public class ExpressionInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- deep contract: ExpressionInfo field shape (analysis result record) ----
    static void testFieldShape() throws Exception {
        Class<?> c = Class.forName("jdk.jshell.ExpressionToTypeInfo$ExpressionInfo");
        java.util.Set<String> fields = new java.util.HashSet<>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fields.add(f.getName());
        // the documented analysis outputs: expression tree + five type-name views + flags
        for (String expected : new String[]{"tree", "isPrimitiveType", "typeName", "accessibleTypeName",
                "declareTypeName", "fullTypeName", "displayTypeName", "isNonVoid", "anonymousClasses"})
            check(fields.contains(expected), "field " + expected);
        check(c.getDeclaredFields().length == 9, "exactly 9 fields");
        // boolean flag types
        for (String flag : new String[]{"isPrimitiveType", "isNonVoid"})
            check(c.getDeclaredField(flag).getType() == boolean.class, "boolean flag " + flag);
    }
    static java.lang.reflect.Field findField(Class<?> c, String n) throws Exception {
        java.lang.reflect.Field f = c.getDeclaredField(n);
        f.setAccessible(true);
        return f;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.jshell.ExpressionToTypeInfo$ExpressionInfo");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getConstructor() != null, "public no-arg ctor");
        check(c.getDeclaredFields().length > 0, "has fields");
        testFieldShape();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
