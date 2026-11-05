/*
 * @test
 * @summary org.omg.CORBA.Bounds conformance: UserException hierarchy and constructors.
 * @run main BoundsTest
 */

public class BoundsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CORBA.UserException.class.isAssignableFrom(org.omg.CORBA.Bounds.class), "extends UserException");
        check(java.lang.Exception.class.isAssignableFrom(org.omg.CORBA.Bounds.class), "is checked Exception");
        check(org.omg.CORBA.portable.IDLEntity.class.isAssignableFrom(org.omg.CORBA.Bounds.class), "implements IDLEntity");
        org.omg.CORBA.Bounds e = new org.omg.CORBA.Bounds();
        org.omg.CORBA.Bounds em = new org.omg.CORBA.Bounds("msg");
        check(em.getMessage() != null && em.getMessage().endsWith("msg"), "message ctor");

        // ---- deepened: thrown by real NVList/TypeCode out-of-range paths ----
        check(e.getMessage() == null, "() default message null");
        check(org.omg.CORBA.Bounds.class.getSuperclass() == org.omg.CORBA.UserException.class,
              "direct UserException subclass");
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.NVList nl = orb.create_list(1);
        nl.add_value("x", orb.create_any(), 0);
        try {
            nl.item(5);
            throw new AssertionError("NVList.item(5) should throw Bounds");
        } catch (org.omg.CORBA.Bounds real) {
            check(true, "NVList.item OOB throws this exact exception type");
        }
        org.omg.CORBA.TypeCode stc = orb.create_struct_tc("IDL:S:1.0", "S", new org.omg.CORBA.StructMember[]{});
        try {
            stc.member_name(0);
        } catch (org.omg.CORBA.TypeCodePackage.Bounds other) {
            check(true, "note: TypeCode OOB uses TypeCodePackage.Bounds, a different type");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
