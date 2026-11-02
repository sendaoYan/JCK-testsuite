/*
 * @test
 * @summary javax.security.auth.login.Configuration.Parameters conformance: configuration parameters marker.
 * @run main ParametersTest
 */

import java.lang.reflect.*;

public class ParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] ns) { for (String n : ns) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasPublicCtor(Class<?> c) { for (Constructor<?> k : c.getConstructors()) return true; return false; }
    static void checkField(Class<?> c, String n) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) { check(true, "field " + n); return; } check(false, "field " + n); }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.security.auth.login.Configuration$Parameters");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
