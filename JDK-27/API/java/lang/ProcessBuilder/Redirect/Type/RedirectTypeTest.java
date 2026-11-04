/*
 * @test
 * @summary java.lang.ProcessBuilder.Redirect.Type conformance: white-box structure + black-box behavior.
 * @run main RedirectTypeTest
 */

import java.lang.ProcessBuilder.Redirect.Type;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RedirectTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Type.class;
        check(Modifier.isFinal(c.getModifiers()), "Type is final");
        check(c.getSuperclass() == Enum.class, "Type extends Enum");
        check(c.isEnum(), "Type is an enum");
        for (Method m : new Method[]{c.getMethod("values"), c.getMethod("valueOf", String.class)}) {
            check(Modifier.isStatic(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                  m.getName() + " is public static");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testConstants() {
        Type[] values = Type.values();
        check(values.length == 5, "exactly 5 redirect types");
        check(java.util.Arrays.toString(values)
              .equals("[PIPE, INHERIT, READ, WRITE, APPEND]"), "declared order PIPE, INHERIT, READ, WRITE, APPEND");
    }

    static void testValueOf() {
        check(Type.valueOf("PIPE") == Type.PIPE, "valueOf PIPE");
        check(Type.valueOf("INHERIT") == Type.INHERIT, "valueOf INHERIT");
        check(Type.valueOf("READ") == Type.READ, "valueOf READ");
        check(Type.valueOf("WRITE") == Type.WRITE, "valueOf WRITE");
        check(Type.valueOf("APPEND") == Type.APPEND, "valueOf APPEND");
        check(Type.PIPE.name().equals("PIPE"), "name is PIPE");
        try {
            Type.valueOf("NO_SUCH_TYPE");
            throw new AssertionError("valueOf(invalid): expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    static void testEnumMechanics() {
        check(Type.values() != Type.values(), "values() returns a fresh array each call");
        check(Type.READ.compareTo(Type.WRITE) < 0, "compareTo order matches declaration order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstants();
        testValueOf();
        testEnumMechanics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
