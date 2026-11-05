/*
 * @test
 * @summary org.omg.CosNaming.NamingContext conformance: interface shape.
 * @run main NamingContextTest
 */

public class NamingContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CosNaming.NamingContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CosNaming.NamingContextOperations.class.isAssignableFrom(c), "extends org.omg.CosNaming.NamingContextOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (batch C, jdk8u372 probed): hierarchy + full method surface ----
        check(org.omg.CosNaming.NamingContext.class.isInterface(), "is an interface");
        check(org.omg.CosNaming.NamingContextOperations.class.isAssignableFrom(org.omg.CosNaming.NamingContext.class),
              "extends NamingContextOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(org.omg.CosNaming.NamingContext.class), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CosNaming.NamingContext.class), "extends IDLEntity");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : org.omg.CosNaming.NamingContext.class.getMethods()) names.add(m.getName());
        for (String expect : new String[]{"bind", "bind_context", "rebind", "rebind_context", "resolve",
                "unbind", "list", "new_context", "bind_new_context", "destroy"})
            check(names.contains(expect), "method surface contains " + expect);
        // checked exceptions on the contract
        try {
            java.lang.reflect.Method bind = org.omg.CosNaming.NamingContext.class.getMethod("bind",
                org.omg.CosNaming.NameComponent[].class, org.omg.CORBA.Object.class);
            java.util.Set<String> exs = new java.util.HashSet<String>();
            for (Class<?> exC : bind.getExceptionTypes()) exs.add(exC.getName());
            for (String e : new String[]{"org.omg.CosNaming.NamingContextPackage.NotFound",
                    "org.omg.CosNaming.NamingContextPackage.CannotProceed",
                    "org.omg.CosNaming.NamingContextPackage.InvalidName",
                    "org.omg.CosNaming.NamingContextPackage.AlreadyBound"})
                check(exs.contains(e), "bind declares checked exception " + e);
        } catch (NoSuchMethodException impossible) {
            throw new AssertionError("bind method missing");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
