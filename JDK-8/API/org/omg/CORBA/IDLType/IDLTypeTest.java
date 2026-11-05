/*
 * @test
 * @summary org.omg.CORBA.IDLType conformance: interface shape.
 * @run main IDLTypeTest
 */

public class IDLTypeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.IDLType");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.IDLTypeOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.IDLTypeOperations");
        check(org.omg.CORBA.IRObject.class.isAssignableFrom(c), "extends org.omg.CORBA.IRObject");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        check(org.omg.CORBA.IDLType.class.getMethods().length == 0
              || org.omg.CORBA.IDLType.class.getDeclaredMethods().length == 0,
              "IDLType adds no operations of its own (marker over its *Operations base)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
