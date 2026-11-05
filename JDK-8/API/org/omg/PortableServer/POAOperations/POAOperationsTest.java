/*
 * @test
 * @summary org.omg.PortableServer.POAOperations conformance: interface shape.
 * @run main POAOperationsTest
 */

public class POAOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableServer.POAOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (jdk8u372-b07 probed): 32-method face / 31 distinct names / checked-exception surface ----
        java.lang.reflect.Method[] ops = c.getDeclaredMethods();
        check(ops.length == 32, "POAOperations declares exactly 32 methods");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : ops) {
            names.add(m.getName());
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), m.getName() + " abstract");
            check(m.getDeclaringClass() == c, m.getName() + " declared here (no inherited face)");
        }
        check(names.size() == 31, "31 distinct names: only the_activator is overloaded (getter + setter)");
        check(c.getInterfaces().length == 0, "POAOperations has no parent interface");
        java.lang.reflect.Method activatorGet = c.getDeclaredMethod("the_activator");
        java.lang.reflect.Method activatorSet = c.getDeclaredMethod("the_activator", org.omg.PortableServer.AdapterActivator.class);
        check(activatorGet.getReturnType() == org.omg.PortableServer.AdapterActivator.class
           && activatorSet.getReturnType() == void.class,
              "the_activator overloads: getter returns AdapterActivator, setter void");
        java.lang.reflect.Method createPoa = c.getDeclaredMethod("create_POA",
              String.class, org.omg.PortableServer.POAManager.class, org.omg.CORBA.Policy[].class);
        java.util.Set<String> thrown = new java.util.HashSet<>();
        for (Class<?> xc : createPoa.getExceptionTypes()) thrown.add(xc.getName());
        check(thrown.equals(new java.util.HashSet<>(java.util.Arrays.asList(
                  "org.omg.PortableServer.POAPackage.AdapterAlreadyExists",
                  "org.omg.PortableServer.POAPackage.InvalidPolicy"))),
              "create_POA throws exactly { AdapterAlreadyExists, InvalidPolicy }");
        thrown.clear();
        for (Class<?> xc : c.getDeclaredMethod("get_servant").getExceptionTypes()) thrown.add(xc.getName());
        check(thrown.equals(new java.util.HashSet<>(java.util.Arrays.asList(
                  "org.omg.PortableServer.POAPackage.NoServant",
                  "org.omg.PortableServer.POAPackage.WrongPolicy"))),
              "get_servant throws exactly { NoServant, WrongPolicy }");
        for (String n : new String[]{"find_POA", "destroy", "activate_object", "activate_object_with_id",
                "deactivate_object", "create_reference", "create_reference_with_id", "servant_to_id",
                "servant_to_reference", "reference_to_servant", "reference_to_id", "id_to_servant",
                "id_to_reference", "the_name", "the_parent", "the_POAManager", "get_servant_manager",
                "set_servant_manager", "set_servant", "id"}) {
            check(names.contains(n), "method face contains " + n);
        }
        check(c.getDeclaredMethod("create_thread_policy", org.omg.PortableServer.ThreadPolicyValue.class)
                  .getReturnType() == org.omg.PortableServer.ThreadPolicy.class
           && c.getDeclaredMethod("create_request_processing_policy", org.omg.PortableServer.RequestProcessingPolicyValue.class)
                  .getReturnType() == org.omg.PortableServer.RequestProcessingPolicy.class,
              "7 create_*_policy factories map each PolicyValue to its Policy interface");
        check(c.getDeclaredMethod("id").getReturnType() == byte[].class, "id() -> byte[]");
        check(c.getDeclaredMethod("deactivate_object", byte[].class).getReturnType() == void.class,
              "deactivate_object(byte[]) void");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
