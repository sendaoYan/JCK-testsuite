/*
 * @test
 * @summary jdk.javadoc.doclet.DocletEnvironment.ModuleMode conformance: enum.
 * @run main ModuleModeTest
 */

import java.lang.reflect.*;


public class ModuleModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.javadoc.doclet.DocletEnvironment$ModuleMode");
        check(c.isEnum(), "isEnum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants() != null && c.getEnumConstants().length > 0, "enum constants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
