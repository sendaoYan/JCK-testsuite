/*
 * @test
 * @summary java.rmi.server.RMIServerSocketFactory conformance: white-box structure + black-box behavior.
 * @run main RMIServerSocketFactoryTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.RMIServerSocketFactory;

public class RMIServerSocketFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RMIServerSocketFactory");
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isInterface(c.getModifiers()), "public interface");
        check(!c.isAnnotation() && !c.isEnum(), "not annotation/enum");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(c.getMethod("createServerSocket", int.class) != null, "createServerSocket(int) declared");
        check(c.getDeclaredMethods().length == 1, "single abstract method");
    }

    static void testBlackBox() throws Exception {
        // functional interface: lambda implementation
        RMIServerSocketFactory f = (port) -> null;
        check(f instanceof RMIServerSocketFactory, "lambda instanceof RMIServerSocketFactory");
        check(f.createServerSocket(0) == null, "lambda createServerSocket returns sentinel");

        // an anonymous implementation records its input
        final int[] captured = new int[1];
        RMIServerSocketFactory g = (port) -> { captured[0] = port; return null; };
        g.createServerSocket(23456);
        check(captured[0] == 23456, "createServerSocket receives port");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
