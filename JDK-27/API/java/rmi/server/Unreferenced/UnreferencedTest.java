/*
 * @test
 * @summary java.rmi.server.Unreferenced conformance: white-box structure + black-box behavior.
 * @run main UnreferencedTest
 */

import java.lang.reflect.Modifier;
import java.rmi.server.Unreferenced;

public class UnreferencedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.server.Unreferenced");
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isInterface(c.getModifiers()), "public interface");
        check(!c.isAnnotation() && !c.isEnum(), "not annotation/enum");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(c.getMethod("unreferenced") != null, "unreferenced() declared");
        check(c.getDeclaredMethods().length == 1, "single abstract method");
    }

    static void testBlackBox() {
        // functional interface: lambda implementation
        Unreferenced u = () -> { };
        check(u instanceof Unreferenced, "lambda instanceof Unreferenced");
        u.unreferenced(); // callable, no exception
        check(true, "unreferenced() callable");

        // a stateful anonymous implementation records the callback
        final boolean[] called = new boolean[1];
        Unreferenced g = () -> { called[0] = true; };
        g.unreferenced();
        check(called[0], "unreferenced() callback invoked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
