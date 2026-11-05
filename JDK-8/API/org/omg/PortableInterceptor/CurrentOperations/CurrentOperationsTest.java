/*
 * @test
 * @summary org.omg.PortableInterceptor.CurrentOperations conformance: interface shape.
 * @run main CurrentOperationsTest
 */

public class CurrentOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.PortableInterceptor.CurrentOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.CurrentOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.CurrentOperations");
        // ---- deepened (jdk8u372-b07 probed): slot access face ----
        java.util.Set<String> currentoperations_m = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : org.omg.PortableInterceptor.CurrentOperations.class.getDeclaredMethods()) currentoperations_m.add(m.getName());
        check(currentoperations_m.equals(new java.util.TreeSet<String>(java.util.Arrays.asList("get_slot", "set_slot"))), "method-name set exact (2 methods)");
        check(org.omg.PortableInterceptor.CurrentOperations.class.getDeclaredMethods().length == 2, "declared method count == 2");
        java.lang.reflect.Method gs = org.omg.PortableInterceptor.CurrentOperations.class.getDeclaredMethod(
            "get_slot", int.class);
        check(gs.getReturnType() == org.omg.CORBA.Any.class, "get_slot(int) returns Any");
        check(gs.getExceptionTypes()[0] == org.omg.PortableInterceptor.InvalidSlot.class,
              "get_slot throws InvalidSlot");
        java.lang.reflect.Method ss = org.omg.PortableInterceptor.CurrentOperations.class.getDeclaredMethod(
            "set_slot", int.class, org.omg.CORBA.Any.class);
        check(ss.getExceptionTypes()[0] == org.omg.PortableInterceptor.InvalidSlot.class,
              "set_slot throws InvalidSlot");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
