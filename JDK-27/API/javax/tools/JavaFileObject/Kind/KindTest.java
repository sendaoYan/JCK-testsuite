/*
 * @test
 * @summary javax.tools.JavaFileObject.Kind conformance: white-box structure + black-box behavior.
 * @run main KindTest
 */

import java.lang.reflect.*;

public class KindTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.tools.JavaFileObject$Kind");
        check(c.isEnum(), "isEnum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 4, "4 enum constants");
        Field ext = c.getDeclaredField("extension");
        check(java.lang.reflect.Modifier.isPublic(ext.getModifiers())
                && java.lang.reflect.Modifier.isFinal(ext.getModifiers()), "public final extension field");
        check(ext.getType() == String.class, "extension field is String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        javax.tools.JavaFileObject.Kind[] all = javax.tools.JavaFileObject.Kind.values();
        check(all.length == 4, "values() length 4");
        check(all[0] == javax.tools.JavaFileObject.Kind.SOURCE, "order SOURCE");
        check(all[1] == javax.tools.JavaFileObject.Kind.CLASS, "order CLASS");
        check(all[2] == javax.tools.JavaFileObject.Kind.HTML, "order HTML");
        check(all[3] == javax.tools.JavaFileObject.Kind.OTHER, "order OTHER");

        check(".java".equals(javax.tools.JavaFileObject.Kind.SOURCE.extension), "SOURCE extension .java");
        check(".class".equals(javax.tools.JavaFileObject.Kind.CLASS.extension), "CLASS extension .class");
        check(".html".equals(javax.tools.JavaFileObject.Kind.HTML.extension), "HTML extension .html");
        check("".equals(javax.tools.JavaFileObject.Kind.OTHER.extension), "OTHER extension empty");

        check("SOURCE".equals(javax.tools.JavaFileObject.Kind.SOURCE.name()), "SOURCE name");
        check("OTHER".equals(javax.tools.JavaFileObject.Kind.OTHER.name()), "OTHER name");
        check(javax.tools.JavaFileObject.Kind.valueOf("CLASS") == javax.tools.JavaFileObject.Kind.CLASS, "valueOf CLASS");
        check(javax.tools.JavaFileObject.Kind.HTML.ordinal() == 2, "ordinal HTML 2");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
