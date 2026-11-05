/*
 * @test
 * @summary org.omg.CosNaming.NamingContextOperations conformance: interface shape.
 * @run main NamingContextOperationsTest
 */

public class NamingContextOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.NamingContextOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (batch C, jdk8u372 probed): operations interface exact surface ----
        check(org.omg.CosNaming.NamingContextOperations.class.isInterface(), "is an interface");
        check(org.omg.CORBA.Object.class.isAssignableFrom(org.omg.CosNaming.NamingContextOperations.class) == false,
              "Operations interface does NOT extend org.omg.CORBA.Object (pure method set)");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CosNaming.NamingContextOperations.class.getDeclaredMethods()) names.add(m.getName());
        check(names.size() == 10, "exactly 10 declared operations, got " + names.size());
        for (String expect : new String[]{"bind", "bind_context", "rebind", "rebind_context", "resolve",
                "unbind", "list", "new_context", "bind_new_context", "destroy"})
            check(names.contains(expect), "declared method " + expect);
        try {
            java.lang.reflect.Method destroy = org.omg.CosNaming.NamingContextOperations.class.getMethod("destroy");
            check(destroy.getExceptionTypes().length == 1
                  && destroy.getExceptionTypes()[0].getName().equals("org.omg.CosNaming.NamingContextPackage.NotEmpty"),
                  "destroy declares only NotEmpty");
            java.lang.reflect.Method list = org.omg.CosNaming.NamingContextOperations.class.getMethod("list",
                int.class, org.omg.CosNaming.BindingListHolder.class, org.omg.CosNaming.BindingIteratorHolder.class);
            check(list.getExceptionTypes().length == 0, "list declares no checked exceptions");
        } catch (NoSuchMethodException impossible) {
            throw new AssertionError("surface incomplete");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
