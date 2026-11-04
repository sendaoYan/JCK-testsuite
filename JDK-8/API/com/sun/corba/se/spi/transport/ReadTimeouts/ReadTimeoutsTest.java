/*
 * @test
 * @summary com.sun.corba.se.spi.transport.ReadTimeouts conformance: type shape via reflection (headless-safe).
 * @run main ReadTimeoutsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ReadTimeoutsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.transport.ReadTimeouts", false, ReadTimeoutsTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("get_backoff_factor"), "method get_backoff_factor");
        check(mn.contains("get_initial_time_to_wait"), "method get_initial_time_to_wait");
        check(mn.contains("get_max_giop_header_time_to_wait"), "method get_max_giop_header_time_to_wait");
        check(mn.contains("get_max_time_to_wait"), "method get_max_time_to_wait");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
