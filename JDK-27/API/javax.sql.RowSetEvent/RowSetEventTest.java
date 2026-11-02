/*
 * @test
 * @summary javax.sql.RowSetEvent conformance: class shape via reflection
 * @run main RowSetEventTest
 */

import java.lang.reflect.Modifier;

public class RowSetEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.RowSetEvent");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass java.util.EventObject");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 0, "0 direct interface(s)");
        check(c.getDeclaredMethods().length == 0, "declared method count 0");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");        check(c.getMethod("getSource") != null, "getSource inherited");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
