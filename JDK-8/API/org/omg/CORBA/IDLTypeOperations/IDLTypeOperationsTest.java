/*
 * @test
 * @summary org.omg.CORBA.IDLTypeOperations conformance: interface shape.
 * @run main IDLTypeOperationsTest
 */

public class IDLTypeOperationsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.IDLTypeOperations");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.IRObjectOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.IRObjectOperations");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.IDLTypeOperations.class.getMethods()) names.add(m.getName());
        java.util.Set<String> expected =
            new java.util.HashSet<String>(java.util.Arrays.asList("type", "def_kind", "destroy"));
        check(names.equals(expected), "getMethods = own type + inherited def_kind/destroy: " + names);
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CORBA.IDLTypeOperations.class.getDeclaredMethods())
            declared.add(m.getName());
        check(declared.equals(new java.util.HashSet<String>(java.util.Arrays.asList("type"))),
              "declares exactly type() itself: " + declared);


        try {
            check(org.omg.CORBA.IDLTypeOperations.class.getMethod("type").getReturnType() == org.omg.CORBA.TypeCode.class,
                  "type() returns TypeCode");
            check(org.omg.CORBA.IRObjectOperations.class.isAssignableFrom(org.omg.CORBA.IDLTypeOperations.class),
                  "IDLTypeOperations extends IRObjectOperations");
        } catch (Exception e) { throw new AssertionError(e); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
