/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExtOperations conformance: interface shape.
 * @run main NamingContextExtOperationsTest
 */

public class NamingContextExtOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.NamingContextExtOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CosNaming.NamingContextOperations.class.isAssignableFrom(c), "extends org.omg.CosNaming.NamingContextOperations");
        // ---- deepened (batch C, jdk8u372 probed): Ext operations surface ----
        check(org.omg.CosNaming.NamingContextOperations.class.isAssignableFrom(org.omg.CosNaming.NamingContextExtOperations.class),
              "NamingContextExtOperations extends NamingContextOperations");
        java.util.Set<String> own = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CosNaming.NamingContextExtOperations.class.getDeclaredMethods()) own.add(m.getName());
        check(own.size() == 4, "exactly 4 own operations: " + own);
        for (String expect : new String[]{"to_string", "to_name", "to_url", "resolve_str"})
            check(own.contains(expect), "own method " + expect);
        try {
            java.lang.reflect.Method toUrl = org.omg.CosNaming.NamingContextExtOperations.class.getMethod("to_url", String.class, String.class);
            java.util.Set<String> exs = new java.util.HashSet<String>();
            for (Class<?> exC : toUrl.getExceptionTypes()) exs.add(exC.getName());
            check(exs.contains("org.omg.CosNaming.NamingContextExtPackage.InvalidAddress")
                  && exs.contains("org.omg.CosNaming.NamingContextPackage.InvalidName"),
                  "to_url declares InvalidAddress + InvalidName");
        } catch (NoSuchMethodException impossible) {
            throw new AssertionError("to_url missing");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
