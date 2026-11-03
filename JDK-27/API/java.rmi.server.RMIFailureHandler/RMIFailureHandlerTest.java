/*
 * @test
 * @summary java.rmi.server.RMIFailureHandler conformance: white-box structure + black-box behavior.
 * @run main RMIFailureHandlerTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.RMIFailureHandler;

public class RMIFailureHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RMIFailureHandler");
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isInterface(c.getModifiers()), "public interface");
        check(!c.isAnnotation() && !c.isEnum(), "not annotation/enum");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(c.getMethod("failure", Exception.class) != null, "failure(Exception) declared");
        check(c.getDeclaredMethods().length == 1, "single abstract method");
    }

    static void testBlackBox() {
        // functional interface: lambda implementation
        RMIFailureHandler h = (ex) -> ex != null;
        check(h instanceof RMIFailureHandler, "lambda instanceof RMIFailureHandler");
        check(h.failure(new Exception("boom")), "failure returns true for non-null");
        check(!h.failure(null), "failure returns false for null");

        // a handler that records the received exception
        final Exception[] captured = new Exception[1];
        RMIFailureHandler g = (ex) -> { captured[0] = ex; return true; };
        Exception e = new Exception("recorded");
        g.failure(e);
        check(captured[0] == e, "failure receives the exception");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
