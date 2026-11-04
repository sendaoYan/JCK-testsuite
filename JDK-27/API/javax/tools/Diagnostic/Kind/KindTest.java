/*
 * @test
 * @summary javax.tools.Diagnostic.Kind conformance: white-box structure + black-box behavior.
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
        Class<?> c = Class.forName("javax.tools.Diagnostic$Kind");
        check(c.isEnum(), "isEnum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length == 5, "5 enum constants");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        javax.tools.Diagnostic.Kind[] all = javax.tools.Diagnostic.Kind.values();
        check(all.length == 5, "values() length 5");
        check(all[0] == javax.tools.Diagnostic.Kind.ERROR, "order ERROR");
        check(all[1] == javax.tools.Diagnostic.Kind.WARNING, "order WARNING");
        check(all[2] == javax.tools.Diagnostic.Kind.MANDATORY_WARNING, "order MANDATORY_WARNING");
        check(all[3] == javax.tools.Diagnostic.Kind.NOTE, "order NOTE");
        check(all[4] == javax.tools.Diagnostic.Kind.OTHER, "order OTHER");

        check("ERROR".equals(javax.tools.Diagnostic.Kind.ERROR.name()), "ERROR name");
        check("WARNING".equals(javax.tools.Diagnostic.Kind.WARNING.name()), "WARNING name");
        check("MANDATORY_WARNING".equals(javax.tools.Diagnostic.Kind.MANDATORY_WARNING.name()), "MANDATORY_WARNING name");
        check("NOTE".equals(javax.tools.Diagnostic.Kind.NOTE.name()), "NOTE name");
        check("OTHER".equals(javax.tools.Diagnostic.Kind.OTHER.name()), "OTHER name");

        check(javax.tools.Diagnostic.Kind.valueOf("ERROR") == javax.tools.Diagnostic.Kind.ERROR, "valueOf ERROR");
        check(javax.tools.Diagnostic.Kind.valueOf("NOTE") == javax.tools.Diagnostic.Kind.NOTE, "valueOf NOTE");
        check(javax.tools.Diagnostic.Kind.ERROR.ordinal() == 0, "ordinal ERROR 0");
        check(javax.tools.Diagnostic.Kind.OTHER.ordinal() == 4, "ordinal OTHER 4");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
