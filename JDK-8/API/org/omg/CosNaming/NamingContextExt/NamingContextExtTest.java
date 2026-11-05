/*
 * @test
 * @summary org.omg.CosNaming.NamingContextExt conformance: interface shape.
 * @run main NamingContextExtTest
 */

public class NamingContextExtTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.NamingContextExt");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CosNaming.NamingContextExtOperations.class.isAssignableFrom(c), "extends org.omg.CosNaming.NamingContextExtOperations");
        check(org.omg.CosNaming.NamingContext.class.isAssignableFrom(c), "extends org.omg.CosNaming.NamingContext");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (batch C, jdk8u372 probed): Ext hierarchy + URL-string methods ----
        check(org.omg.CosNaming.NamingContext.class.isAssignableFrom(org.omg.CosNaming.NamingContextExt.class),
              "NamingContextExt extends NamingContext");
        check(org.omg.CosNaming.NamingContextExtOperations.class.isAssignableFrom(org.omg.CosNaming.NamingContextExt.class),
              "extends NamingContextExtOperations");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NamingContextExt.class), "extends IDLEntity");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CosNaming.NamingContextExt.class.getMethods()) names.add(m.getName());
        for (String expect : new String[]{"to_string", "to_name", "to_url", "resolve_str"})
            check(names.contains(expect), "method surface contains " + expect);
        try {
            java.lang.reflect.Method toName = org.omg.CosNaming.NamingContextExt.class.getMethod("to_name", String.class);
            check(toName.getReturnType() == org.omg.CosNaming.NameComponent[].class, "to_name returns NameComponent[]");
            java.util.Set<String> exs = new java.util.HashSet<String>();
            for (Class<?> exC : toName.getExceptionTypes()) exs.add(exC.getName());
            check(exs.contains("org.omg.CosNaming.NamingContextPackage.InvalidName"), "to_name declares InvalidName");
        } catch (NoSuchMethodException impossible) {
            throw new AssertionError("to_name missing");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
