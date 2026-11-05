/*
 * @test
 * @summary org.omg.CosNaming.BindingIteratorHelper conformance: helper static id/type/narrow.
 * @run main BindingIteratorHelperTest
 */

public class BindingIteratorHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.BindingIteratorHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.BindingIteratorHelper.type() != null, "type() non-null");
        check(org.omg.CosNaming.BindingIteratorHelper.narrow(null) == null, "narrow(null) returns null");
        // ---- deepened (batch C, jdk8u372 probed): objref helper, nil handling, narrow ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.BindingIteratorHelper.id().equals("IDL:omg.org/CosNaming/BindingIterator:1.0"), "id() exact repository id");
        org.omg.CORBA.TypeCode tc = org.omg.CosNaming.BindingIteratorHelper.type();
        check(tc.kind() == org.omg.CORBA.TCKind.tk_objref, "type().kind() == tk_objref");
        check(tc.name().equals("BindingIterator"), "type().name() == NAME");
        check(tc.id().equals(org.omg.CosNaming.BindingIteratorHelper.id()), "type().id() equals Helper.id()");
        org.omg.CORBA.Any anyNull = orb.create_any();
        org.omg.CosNaming.BindingIteratorHelper.insert(anyNull, null);
        check(anyNull.type().kind() == org.omg.CORBA.TCKind.tk_objref, "insert(null objref) legal, Any kind tk_objref");
        check(org.omg.CosNaming.BindingIteratorHelper.extract(anyNull) == null, "extract returns null for nil reference");
        org.omg.CORBA.portable.OutputStream dpOs = orb.create_output_stream();
        org.omg.CosNaming.BindingIteratorHelper.write(dpOs, null);
        check(org.omg.CosNaming.BindingIteratorHelper.read(dpOs.create_input_stream()) == null, "CDR write(nil)/read -> null");
        check(org.omg.CosNaming.BindingIteratorHelper.narrow(null) == null, "narrow(null) returns null without throwing");
        org.omg.CORBA.Any wrongAny = orb.create_any();
        wrongAny.insert_long(3);
        try {
            org.omg.CosNaming.BindingIteratorHelper.extract(wrongAny);
            throw new AssertionError("extract over tk_long Any must throw");
        } catch (org.omg.CORBA.MARSHAL expected) {
            check(true, "extract over tk_long Any -> org.omg.CORBA.MARSHAL");
        }

        System.out.println("PASS (" + passed + " assertions)");
    }
}
