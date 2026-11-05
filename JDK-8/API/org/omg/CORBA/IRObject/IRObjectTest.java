/*
 * @test
 * @summary org.omg.CORBA.IRObject conformance: interface shape.
 * @run main IRObjectTest
 */

public class IRObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.IRObject");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(org.omg.CORBA.IRObjectOperations.class.isAssignableFrom(c), "extends org.omg.CORBA.IRObjectOperations");
        check(org.omg.CORBA.Object.class.isAssignableFrom(c), "extends org.omg.CORBA.Object");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(c), "extends org.omg.CORBA.portable.IDLEntity");

        // ---- deepened: exact method surface (javap-verified on jdk8u372) ----
        check(org.omg.CORBA.IRObject.class.getMethods().length == 0
              || org.omg.CORBA.IRObject.class.getDeclaredMethods().length == 0,
              "IRObject adds no operations of its own (marker over its *Operations base)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
