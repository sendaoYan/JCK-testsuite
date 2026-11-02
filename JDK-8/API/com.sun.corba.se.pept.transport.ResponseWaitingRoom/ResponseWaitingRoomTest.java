/*
 * @test
 * @summary com.sun.corba.se.pept.transport.ResponseWaitingRoom conformance: type shape via reflection (headless-safe).
 * @run main ResponseWaitingRoomTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ResponseWaitingRoomTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.pept.transport.ResponseWaitingRoom", false, ResponseWaitingRoomTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("numberRegistered"), "method numberRegistered");
        check(mn.contains("registerWaiter"), "method registerWaiter");
        check(mn.contains("responseReceived"), "method responseReceived");
        check(mn.contains("unregisterWaiter"), "method unregisterWaiter");
        check(mn.contains("waitForResponse"), "method waitForResponse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
