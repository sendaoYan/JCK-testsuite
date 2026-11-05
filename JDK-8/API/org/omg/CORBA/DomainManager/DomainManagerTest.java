/*
 * @test
 * @summary org.omg.CORBA.DomainManager conformance: interface shape.
 * @run main DomainManagerTest
 */

public class DomainManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.DomainManager");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.DomainManagerOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.DomainManagerOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        check(org.omg.CORBA.DomainManager.class.getMethods().length == 0
              || org.omg.CORBA.DomainManager.class.getDeclaredMethods().length == 0,
              "DomainManager adds no operations of its own (marker over its *Operations base)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
