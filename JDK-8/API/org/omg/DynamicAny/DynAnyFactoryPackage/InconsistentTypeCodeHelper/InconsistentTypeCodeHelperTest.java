/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper conformance: helper static id/type/narrow.
 * @run main InconsistentTypeCodeHelperTest
 */

public class InconsistentTypeCodeHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.type() != null, "type() non-null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): helper identity + wire channels ----
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.id()
              .equals("IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0"), "id() exact");
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.type().kind() == org.omg.CORBA.TCKind.tk_except, "type() tk_except");
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.type().name().equals("InconsistentTypeCode"), "type().name()");
        org.omg.CORBA.Any dpA = orb.create_any();
        org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.insert(dpA,
            new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode("bad"));
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.extract(dpA).getMessage()
              .equals("IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0"), "Any round-trip (reason dropped)");
        org.omg.CORBA.portable.OutputStream dpO = orb.create_output_stream();
        org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.write(dpO,
            new org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode());
        check(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper.read(dpO.create_input_stream()).getMessage()
              .equals("IDL:omg.org/DynamicAny/DynAnyFactory/InconsistentTypeCode:1.0"), "CDR stream round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
