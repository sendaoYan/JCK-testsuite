/*
 * @test
 * @summary java.lang.Void conformance: white-box structure + black-box behavior.
 * @run main VoidTest
 */

import java.lang.reflect.Modifier;

public class VoidTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Void");
        check(Modifier.isFinal(c.getModifiers()), "Void is final");
        check(!c.isInterface(), "Void is a class");
        check(c.getField("TYPE") != null, "Void.TYPE field present");
        check(c.getConstructors().length == 0, "Void has no public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testType() {
        check(Void.TYPE == void.class, "Void.TYPE == void.class");
        check(Void.TYPE.getName().equals("void"), "Void.TYPE name is \"void\"");
        check(Void.TYPE.isPrimitive(), "Void.TYPE is primitive");
        check(Void.class != Void.TYPE, "Void.class (wrapper) != Void.TYPE (primitive)");
        check(Void.class.getName().equals("java.lang.Void"), "Void.class name is java.lang.Void");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testType();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
