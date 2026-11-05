/*
 * @test
 * @summary org.omg.CORBA.ExceptionList conformance: abstract class shape.
 * @run main ExceptionListTest
 */

public class ExceptionListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.omg.CORBA.ExceptionList");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");

        // real instance from the offline ORB factory
        org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.CORBA.ExceptionList el = orb.create_exception_list();
        check(el != null, "create_exception_list works offline");

        // ---- empty list ----
        check(el.count() == 0, "fresh ExceptionList count 0");

        // ---- add / item / count with dynamic TypeCodes ----
        org.omg.CORBA.TypeCode tc1 = orb.get_primitive_tc(org.omg.CORBA.TCKind.tk_long);
        org.omg.CORBA.TypeCode tc2 = orb.create_enum_tc("IDL:E:1.0", "E", new String[]{"A"});
        org.omg.CORBA.TypeCode tc3 = orb.create_struct_tc("IDL:S:1.0", "S",
                new org.omg.CORBA.StructMember[]{});
        el.add(tc1);
        check(el.count() == 1, "count after first add");
        el.add(tc2);
        el.add(tc3);
        check(el.count() == 3, "count after three adds");
        check(el.item(0).equal(tc1), "item(0) TypeCode equal to added");
        check(el.item(1).equal(tc2), "item(1) TypeCode equal to added");
        check(el.item(2).kind() == org.omg.CORBA.TCKind.tk_struct, "item(2) kind readback");

        // ---- remove decrements and shifts ----
        el.remove(0);
        check(el.count() == 2, "count after remove");
        check(el.item(0).equal(tc2), "old item(1) shifted to item(0)");
        check(el.item(1).equal(tc3), "old item(2) shifted to item(1)");

        // ---- out-of-bounds -> org.omg.CORBA.Bounds ----
        try {
            el.item(2);
            throw new AssertionError("item(2) should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "item(count) -> org.omg.CORBA.Bounds");
        }
        try {
            el.item(-1);
            throw new AssertionError("item(-1) should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "item(-1) -> org.omg.CORBA.Bounds");
        }
        try {
            el.remove(2);
            throw new AssertionError("remove(2) should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "remove(count) -> org.omg.CORBA.Bounds");
        }

        // drain to empty
        el.remove(0);
        el.remove(0);
        check(el.count() == 0, "list drains to zero");
        try {
            el.item(0);
            throw new AssertionError("item(0) on empty list should throw Bounds");
        } catch (org.omg.CORBA.Bounds expected) {
            check(true, "drained item(0) -> Bounds");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
