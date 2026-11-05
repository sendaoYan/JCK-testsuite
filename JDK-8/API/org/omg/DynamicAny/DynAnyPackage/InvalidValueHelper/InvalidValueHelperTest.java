/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper conformance: helper static id/type/narrow.
 * @run main InvalidValueHelperTest
 */

public class InvalidValueHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.type() != null, "type() non-null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): helper identity + wire channels ----
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.id().equals("IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0"), "id() exact");
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.type().kind() == org.omg.CORBA.TCKind.tk_except, "type() tk_except");
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.type().name().equals("InvalidValue"), "type().name() InvalidValue");
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.type().member_count() == 0, "member_count 0");
        org.omg.CORBA.Any dpA = orb.create_any();
        org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.insert(dpA,
            new org.omg.DynamicAny.DynAnyPackage.InvalidValue("why"));
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.extract(dpA).getMessage()
              .equals("IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0"), "Any round-trip (reason dropped)");
        check(dpA.type().equal(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.type()), "Any type equal helper type()");
        org.omg.CORBA.portable.OutputStream dpO = orb.create_output_stream();
        org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.write(dpO,
            new org.omg.DynamicAny.DynAnyPackage.InvalidValue());
        check(org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper.read(dpO.create_input_stream()).getMessage()
              .equals("IDL:omg.org/DynamicAny/DynAny/InvalidValue:1.0"), "CDR stream round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
