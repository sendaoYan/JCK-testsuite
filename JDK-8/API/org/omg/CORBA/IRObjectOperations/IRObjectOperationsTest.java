/*
 * @test
 * @summary org.omg.CORBA.IRObjectOperations conformance: interface shape.
 * @run main IRObjectOperationsTest
 */

public class IRObjectOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.IRObjectOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.IRObjectOperations.class.getMethods()) names.add(m.getName());
        java.util.Set<String> expected = new java.util.HashSet<String>(java.util.Arrays.asList("def_kind","destroy"));
        check(names.equals(expected), "IRObjectOperations declares exactly def_kind,destroy: " + names);


        try {
            check(org.omg.CORBA.IRObjectOperations.class.getMethod("def_kind").getReturnType()
                    == org.omg.CORBA.DefinitionKind.class, "def_kind() returns DefinitionKind");
        } catch (Exception e) { throw new AssertionError(e); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
