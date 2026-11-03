/*
 * @test
 * @summary java.rmi.registry.Registry conformance: white-box structure + black-box behavior.
 * @run main RegistryTest
 */

import java.rmi.registry.Registry;
import java.rmi.Remote;

public class RegistryTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.registry.Registry");
        check(c.isInterface(), "is interface");
        check(java.rmi.Remote.class.isAssignableFrom(c), "extends Remote");
        java.lang.reflect.Field f = c.getField("REGISTRY_PORT");
        check(java.lang.reflect.Modifier.isStatic(f.getModifiers()) && java.lang.reflect.Modifier.isFinal(f.getModifiers()), "REGISTRY_PORT static final");
        java.util.TreeSet<String> m = new java.util.TreeSet<>();
        for (java.lang.reflect.Method mm : c.getDeclaredMethods()) m.add(mm.getName());
        check(m.equals(java.util.Set.of("bind", "list", "lookup", "rebind", "unbind")), "methods");

    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(Registry.REGISTRY_PORT == 1099, "REGISTRY_PORT == 1099");

        class Impl implements Registry {
            java.util.Map<String, Remote> map = new java.util.HashMap<>();
            public Remote lookup(String n) throws java.rmi.RemoteException, java.rmi.NotBoundException, java.rmi.AccessException { return map.get(n); }
            public void bind(String n, Remote o) throws java.rmi.RemoteException, java.rmi.AlreadyBoundException, java.rmi.AccessException { map.put(n, o); }
            public void unbind(String n) throws java.rmi.RemoteException, java.rmi.NotBoundException, java.rmi.AccessException { map.remove(n); }
            public void rebind(String n, Remote o) throws java.rmi.RemoteException, java.rmi.AccessException { map.put(n, o); }
            public String[] list() throws java.rmi.RemoteException, java.rmi.AccessException { return map.keySet().toArray(new String[0]); }
        }
        Registry reg = new Impl();
        check(reg instanceof Remote, "Registry impl instanceof Remote");
        Remote target = new Remote() { };
        reg.bind("svc", target);
        check(reg.lookup("svc") == target, "bind -> lookup");
        String[] names = reg.list();
        check(names.length == 1 && "svc".equals(names[0]), "list after bind");
        reg.rebind("svc", new Remote() { });
        check(reg.lookup("svc") != target, "rebind replaces");
        reg.unbind("svc");
        check(reg.list().length == 0, "unbind empties registry");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
