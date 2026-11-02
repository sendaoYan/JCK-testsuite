/*
 * @test
 * @summary javax.sql.rowset.serial.SerialRef conformance: class shape via reflection
 * @run main SerialRefTest
 */

import java.lang.reflect.Modifier;

public class SerialRefTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.serial.SerialRef");
        check(c != null, "loads");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 3, "3 direct interface(s)");
        check(ifs[0].getName().equals("java.sql.Ref"), "interface 0");
        check(ifs[1].getName().equals("java.io.Serializable"), "interface 1");
        check(ifs[2].getName().equals("java.lang.Cloneable"), "interface 2");
        check(c.getDeclaredMethods().length == 9, "declared method count 9");
        check(c.getDeclaredConstructors().length == 1, "constructor count 1");        System.out.println("PASS (" + passed + " assertions)");
    }
}
