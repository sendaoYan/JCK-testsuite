/*
 * @test
 * @summary org.omg.PortableServer.POAManagerOperations conformance: interface shape.
 * @run main POAManagerOperationsTest
 */

public class POAManagerOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.POAManagerOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): 5-method state machine face ----
        java.lang.reflect.Method[] ops = c.getDeclaredMethods();
        check(ops.length == 5, "exactly 5 methods: activate/hold_requests/discard_requests/deactivate/get_state");
        check(c.getInterfaces().length == 0, "no parent interface");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : ops) names.add(m.getName());
        check(names.equals(new java.util.HashSet<>(java.util.Arrays.asList(
                  "activate", "hold_requests", "discard_requests", "deactivate", "get_state"))),
              "POAManagerOperations method name set exact");
        check(c.getDeclaredMethod("get_state").getReturnType() == org.omg.PortableServer.POAManagerPackage.State.class,
              "get_state() -> POAManagerPackage.State");
        for (String n : new String[]{"activate", "hold_requests", "discard_requests", "deactivate"}) {
            java.util.Set<String> thrown = new java.util.HashSet<>();
            for (Class<?> xc : c.getDeclaredMethod(n,
                    n.equals("hold_requests") ? new Class<?>[]{boolean.class}
                    : n.equals("discard_requests") ? new Class<?>[]{boolean.class}
                    : n.equals("deactivate") ? new Class<?>[]{boolean.class, boolean.class}
                    : new Class<?>[]{}).getExceptionTypes()) thrown.add(xc.getName());
            check(thrown.equals(java.util.Collections.singleton("org.omg.PortableServer.POAManagerPackage.AdapterInactive")),
                  n + " throws exactly AdapterInactive");
        }
        check(c.getDeclaredMethod("hold_requests", boolean.class).getParameterTypes().length == 1
           && c.getDeclaredMethod("deactivate", boolean.class, boolean.class).getParameterTypes().length == 2
           && c.getDeclaredMethod("activate").getParameterTypes().length == 0,
              "arity exact: activate()=0, hold/discard=1 boolean, deactivate=2 booleans");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
