/*
 * @test
 * @summary javax.sql.StatementEvent conformance: class shape via reflection
 * @run main StatementEventTest
 */

import java.lang.reflect.Modifier;

public class StatementEventTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.StatementEvent");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.EventObject"), "superclass java.util.EventObject");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 0, "0 direct interface(s)");
        check(c.getDeclaredMethods().length == 2, "declared method count 2");
        check(c.getDeclaredConstructors().length == 2, "constructor count 2");        check(c.getDeclaredMethod("getStatement") != null, "getStatement present");
        check(c.getDeclaredMethod("getSQLException") != null, "getSQLException present");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
