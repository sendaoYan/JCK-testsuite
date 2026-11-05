/*
 * @test
 * @summary org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper conformance: helper static id/type/narrow.
 * @run main TypeMismatchHelperTest
 */

public class TypeMismatchHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.type() != null, "type() non-null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): helper identity + wire channels ----
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.id().equals("IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0"), "id() exact");
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.type().kind() == org.omg.CORBA.TCKind.tk_except, "type() tk_except");
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.type().name().equals("TypeMismatch"), "type().name() TypeMismatch");
        org.omg.CORBA.Any dpA = orb.create_any();
        org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.insert(dpA,
            new org.omg.DynamicAny.DynAnyPackage.TypeMismatch("kind"));
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.extract(dpA).getMessage()
              .equals("IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0"), "Any round-trip (reason dropped)");
        org.omg.CORBA.portable.OutputStream dpO = orb.create_output_stream();
        org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.write(dpO,
            new org.omg.DynamicAny.DynAnyPackage.TypeMismatch());
        check(org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper.read(dpO.create_input_stream()).getMessage()
              .equals("IDL:omg.org/DynamicAny/DynAny/TypeMismatch:1.0"), "CDR stream round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
