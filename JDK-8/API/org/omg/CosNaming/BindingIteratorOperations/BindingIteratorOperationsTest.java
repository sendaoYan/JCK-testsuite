/*
 * @test
 * @summary org.omg.CosNaming.BindingIteratorOperations conformance: interface shape.
 * @run main BindingIteratorOperationsTest
 */

public class BindingIteratorOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.BindingIteratorOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        // ---- deepened (batch C, jdk8u372 probed): exact surface ----
        java.util.Set<String> own = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CosNaming.BindingIteratorOperations.class.getDeclaredMethods()) own.add(m.getName());
        check(own.size() == 3, "exactly 3 operations: " + own);
        for (String expect : new String[]{"next_one", "next_n", "destroy"})
            check(own.contains(expect), "own method " + expect);
        try {
            java.lang.reflect.Method destroy = org.omg.CosNaming.BindingIteratorOperations.class.getMethod("destroy");
            check(destroy.getExceptionTypes().length == 0, "destroy declares no checked exceptions");
        } catch (NoSuchMethodException impossible) {
            throw new AssertionError("destroy missing");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
