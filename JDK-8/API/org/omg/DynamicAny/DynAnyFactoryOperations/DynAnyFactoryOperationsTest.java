/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyFactoryOperations conformance: interface shape.
 * @run main DynAnyFactoryOperationsTest
 */

public class DynAnyFactoryOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.DynamicAny.DynAnyFactoryOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (batch C, jdk8u372 probed): exact operations surface ----
        java.util.Set<String> dpOwn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.DynamicAny.DynAnyFactoryOperations.class.getDeclaredMethods()) dpOwn.add(m.getName());
        check(dpOwn.size() == 2, "exactly 2 operations: " + dpOwn);
        check(dpOwn.contains("create_dyn_any") && dpOwn.contains("create_dyn_any_from_type_code"), "both factory operations present");
        check(org.omg.DynamicAny.DynAnyOperations.class.isAssignableFrom(org.omg.DynamicAny.DynAnyFactoryOperations.class) == false,
              "DynAnyFactoryOperations does not extend DynAnyOperations (independent interface)");
        try {
            java.lang.reflect.Method m = org.omg.DynamicAny.DynAnyFactoryOperations.class.getMethod("create_dyn_any", org.omg.CORBA.Any.class);
            check(java.util.Arrays.asList(m.getExceptionTypes()).toString().contains("InconsistentTypeCode"),
                  "create_dyn_any declares InconsistentTypeCode");
        } catch (NoSuchMethodException impossible) {
            throw new AssertionError("create_dyn_any missing");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
