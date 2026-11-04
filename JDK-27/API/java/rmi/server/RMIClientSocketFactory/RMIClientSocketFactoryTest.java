/*
 * @test
 * @summary java.rmi.server.RMIClientSocketFactory conformance: white-box structure + black-box behavior.
 * @run main RMIClientSocketFactoryTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.RMIClientSocketFactory;

public class RMIClientSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RMIClientSocketFactory");
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isInterface(c.getModifiers()), "public interface");
        check(!c.isAnnotation() && !c.isEnum(), "not annotation/enum");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(c.getMethod("createSocket", String.class, int.class) != null, "createSocket(String,int) declared");
        check(c.getDeclaredMethods().length == 1, "single abstract method");
    }

    static void testBlackBox() throws Exception {
        // functional interface: lambda implementation
        RMIClientSocketFactory f = (host, port) -> null;
        check(f instanceof RMIClientSocketFactory, "lambda instanceof RMIClientSocketFactory");
        check(f.createSocket("example.invalid", 1) == null, "lambda createSocket returns sentinel");

        // an anonymous implementation records its inputs
        final String[] captured = new String[2];
        RMIClientSocketFactory g = (host, port) -> { captured[0] = host; captured[1] = String.valueOf(port); return null; };
        g.createSocket("localhost", 12345);
        check("localhost".equals(captured[0]) && "12345".equals(captured[1]), "createSocket receives host and port");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
