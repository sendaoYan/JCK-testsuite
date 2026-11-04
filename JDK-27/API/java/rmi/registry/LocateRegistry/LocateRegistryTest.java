/*
 * @test
 * @summary java.rmi.registry.LocateRegistry conformance: white-box structure + black-box behavior (no connection).
 * @run main LocateRegistryTest
 */

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LocateRegistryTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.registry.LocateRegistry");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) && !c.isInterface(), "final class");
        check(c.getDeclaredConstructors().length == 1, "single ctor");
        check(java.lang.reflect.Modifier.isPrivate(c.getDeclaredConstructors()[0].getModifiers()), "private ctor");
        java.util.TreeSet<String> m = new java.util.TreeSet<>();
        for (java.lang.reflect.Method mm : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isStatic(mm.getModifiers())) m.add(mm.getName());
        check(m.equals(java.util.Set.of("createRegistry", "getRegistry")), "static methods");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Registry r = LocateRegistry.getRegistry();
        check(r != null, "getRegistry() non-null");
        check(r instanceof Registry, "getRegistry() instanceof Registry");
        check(LocateRegistry.getRegistry(1099) != null, "getRegistry(port)");
        check(LocateRegistry.getRegistry("localhost") != null, "getRegistry(host)");
        check(LocateRegistry.getRegistry("localhost", 1099) != null, "getRegistry(host,port)");
        check(LocateRegistry.getRegistry("localhost", 1099, null) != null, "getRegistry(host,port,csf)");
        check(LocateRegistry.getRegistry(0) != null, "getRegistry(0) coerced to default port");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
