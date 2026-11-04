/*
 * @test
 * @summary java.rmi.registry.RegistryHandler conformance: white-box structure + black-box behavior.
 * @run main RegistryHandlerTest
 */

import java.rmi.registry.RegistryHandler;
import java.rmi.registry.Registry;

public class RegistryHandlerTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.registry.RegistryHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no super interfaces");
        check(c.getAnnotation(Deprecated.class) != null, "deprecated");
        java.util.TreeSet<String> m = new java.util.TreeSet<>();
        for (java.lang.reflect.Method mm : c.getDeclaredMethods()) m.add(mm.getName());
        check(m.equals(java.util.Set.of("registryImpl", "registryStub")), "methods");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        class Impl implements RegistryHandler {
            public Registry registryStub(String host, int port) { return null; }
            public Registry registryImpl(int port) { return null; }
        }
        RegistryHandler h = new Impl();
        check(h != null, "impl non-null");
        check(h instanceof RegistryHandler, "instanceof RegistryHandler");
        check(h.registryStub("localhost", 1099) == null, "registryStub callable");
        check(h.registryImpl(1099) == null, "registryImpl callable");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
