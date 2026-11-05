/*
 * @test
 * @summary org.omg.SendingContext.RunTime conformance: interface shape.
 * @run main RunTimeTest
 */

public class RunTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.SendingContext.RunTime");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.SendingContext.RunTimeOperations.class.isAssignableFrom(c), "extends org.omg.SendingContext.RunTimeOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");
        // ---- deepened (jdk8u372-b07 probed): SendingContext::RunTime shape ----
        check(c.getDeclaredMethods().length == 0, "RunTime declares no methods of its own (pure conformance interface)");
        check(c.getInterfaces().length == 3, "extends exactly 3 interfaces");
        check(java.util.Arrays.asList(c.getInterfaces()).containsAll(java.util.Arrays.asList(
                  org.omg.SendingContext.RunTimeOperations.class, org.omg.CORBA.Object.class,
                  org.omg.CORBA.portable.IDLEntity.class)), "RunTimeOperations + CORBA.Object + IDLEntity");
        // org.omg.SendingContext.Entity (the base of the spec's RunTime) is NOT materialized in jdk8u372
        boolean entityAbsent = true;
        try { Class.forName("org.omg.SendingContext.Entity"); entityAbsent = false; } catch (ClassNotFoundException cn) { }
        check(entityAbsent, "org.omg.SendingContext.Entity not materialized in jdk8u372 rt.jar");
        check(org.omg.SendingContext.RunTimeOperations.class.getDeclaredMethods().length == 0,
              "RunTimeOperations is an empty operations interface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
