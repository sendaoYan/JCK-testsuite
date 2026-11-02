/*
 * @test
 * @summary jdk.jfr.FlightRecorderPermission conformance: type shape.
 * @run main FlightRecorderPermissionTest
 */

import java.lang.reflect.Modifier;

public class FlightRecorderPermissionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.FlightRecorderPermission");
        check(c != null, "loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) == true, "public");
        check(Modifier.isProtected(m) == false, "protected");
        check(c.isInterface() == false, "isInterface");
        check(c.isAnnotation() == false, "isAnnotation");
        check(c.isEnum() == false, "isEnum");
        check(Modifier.isAbstract(m) == false, "abstract");
        check(Modifier.isFinal(m) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.BasicPermission"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
