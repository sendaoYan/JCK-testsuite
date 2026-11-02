/*
 * @test
 * @summary java.sql.Driver conformance: interface shape, supertypes and constants
 * @run main DriverTest
 */

import java.lang.reflect.Modifier;

public class DriverTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Driver");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 0, "0 direct superinterface(s)");
        check(c.getDeclaredMethods().length == 7, "declared method count 7");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
