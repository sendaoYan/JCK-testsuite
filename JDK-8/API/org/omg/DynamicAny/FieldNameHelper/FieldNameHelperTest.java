/*
 * @test
 * @summary org.omg.DynamicAny.FieldNameHelper conformance: helper static id/type/narrow.
 * @run main FieldNameHelperTest
 */

public class FieldNameHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.DynamicAny.FieldNameHelper.id() != null, "id() non-null");
        check(org.omg.DynamicAny.FieldNameHelper.type() != null, "type() non-null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        // ---- deepened (batch C, jdk8u372 probed): alias of string + dual wire channels ----
        check(org.omg.DynamicAny.FieldNameHelper.id().equals("IDL:omg.org/DynamicAny/FieldName:1.0"), "id() exact");
        org.omg.CORBA.TypeCode dpTc = org.omg.DynamicAny.FieldNameHelper.type();
        check(dpTc.kind() == org.omg.CORBA.TCKind.tk_alias, "type() tk_alias");
        check(dpTc.name().equals("FieldName"), "alias name FieldName");
        check(dpTc.content_type().kind() == org.omg.CORBA.TCKind.tk_string, "alias content tk_string");
        org.omg.CORBA.Any dpA = orb.create_any();
        org.omg.DynamicAny.FieldNameHelper.insert(dpA, "fld");
        check(org.omg.DynamicAny.FieldNameHelper.extract(dpA).equals("fld"), "Any round-trip");
        check(dpA.type().kind() == org.omg.CORBA.TCKind.tk_alias, "inserted Any carries the alias type");
        org.omg.CORBA.portable.OutputStream dpO = orb.create_output_stream();
        org.omg.DynamicAny.FieldNameHelper.write(dpO, "fld2");
        check(org.omg.DynamicAny.FieldNameHelper.read(dpO.create_input_stream()).equals("fld2"), "CDR round-trip");
        org.omg.CORBA.Any dpAe = orb.create_any();
        org.omg.DynamicAny.FieldNameHelper.insert(dpAe, "");
        check(org.omg.DynamicAny.FieldNameHelper.extract(dpAe).isEmpty(), "empty FieldName round-trip");
        org.omg.CORBA.portable.OutputStream dpOl = orb.create_output_stream();
        org.omg.DynamicAny.FieldNameHelper.write(dpOl, "\u00e9");
        check(org.omg.DynamicAny.FieldNameHelper.read(dpOl.create_input_stream()).equals("\u00e9"),
              "Latin-1 e-acute round-trip (offline negotiated codeset ISO-8859-1)");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
