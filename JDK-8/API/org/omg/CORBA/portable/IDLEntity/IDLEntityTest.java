/*
 * @test
 * @summary org.omg.CORBA.portable.IDLEntity conformance: interface shape.
 * @run main IDLEntityTest
 */

public class IDLEntityTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.portable.IDLEntity");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interfaces have null superclass");
        check(java.io.Serializable.class.isAssignableFrom(c), "extends java.io.Serializable");

        // ---- deepened: marker interface implemented across the IDL mapping ----
        check(c.getDeclaredMethods().length == 0, "declares no methods (pure marker)");
        check(c.isAssignableFrom(org.omg.CORBA.Any.class), "Any implements IDLEntity");
        check(c.isAssignableFrom(org.omg.CORBA.TypeCode.class), "TypeCode implements IDLEntity");
        check(!c.isAssignableFrom(org.omg.CORBA.TCKind.class),
              "TCKind does NOT implement IDLEntity (jdk8u372 quirk)");
        check(c.isAssignableFrom(org.omg.CORBA.CompletionStatus.class),
              "CompletionStatus implements IDLEntity");
        check(c.isAssignableFrom(org.omg.CORBA.StructMember.class), "StructMember implements IDLEntity");
        check(c.isAssignableFrom(org.omg.CORBA.UserException.class), "UserException implements IDLEntity");
        check(c.isAssignableFrom(org.omg.CORBA.ServiceInformation.class), "ServiceInformation implements IDLEntity");
        check(c.isAssignableFrom(org.omg.CORBA.portable.ValueBase.class), "ValueBase extends IDLEntity");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
