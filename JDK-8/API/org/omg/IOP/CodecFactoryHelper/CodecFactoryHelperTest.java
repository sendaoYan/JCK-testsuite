/*
 * @test
 * @summary org.omg.IOP.CodecFactoryHelper conformance: helper static id/type/narrow.
 * @run main CodecFactoryHelperTest
 */

public class CodecFactoryHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.IOP.CodecFactoryHelper.id() != null, "id() non-null");
        check(org.omg.IOP.CodecFactoryHelper.type() != null, "type() non-null");
        check(org.omg.IOP.CodecFactoryHelper.narrow(null) == null, "narrow(null) returns null");
        // ---- deepened (jdk8u372-b07 probed): offline CodecFactory channel ----
        check(org.omg.IOP.CodecFactoryHelper.id().equals("IDL:omg.org/IOP/CodecFactory:1.0"), "id() exact");
        check(org.omg.IOP.CodecFactoryHelper.type().kind() == org.omg.CORBA.TCKind.tk_objref, "type().kind() == tk_objref");
        check(org.omg.IOP.CodecFactoryHelper.narrow(null) == null, "narrow(null) returns null (no throw)");
        check(org.omg.IOP.CodecFactoryHelper.unchecked_narrow(null) == null, "unchecked_narrow(null) returns null");
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        org.omg.IOP.CodecFactory cf = org.omg.IOP.CodecFactoryHelper.narrow(
            (org.omg.CORBA.Object) orb.resolve_initial_references("CodecFactory"));
        check(cf != null, "resolve_initial_references(CodecFactory) narrows non-null offline");
        check(cf instanceof org.omg.CORBA.Object, "CodecFactory is a CORBA object");
        org.omg.IOP.Codec c = cf.create_codec(
            new org.omg.IOP.Encoding(org.omg.IOP.ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 2));
        check(c != null, "create_codec(0,1,2) returns Codec through narrowed factory");
        boolean marshal = false;
        try { org.omg.IOP.CodecFactoryHelper.extract(orb.create_any()); }
        catch (org.omg.CORBA.MARSHAL m) { marshal = true; }
        check(marshal, "extract(empty any) -> MARSHAL");
        // inserting the local (unconnected) CodecFactory into an Any fails: write_Object rejects local objects
        boolean marshalLocal = false;
        try { org.omg.IOP.CodecFactoryHelper.insert(orb.create_any(), cf); }
        catch (org.omg.CORBA.MARSHAL m) { marshalLocal = true; }
        check(marshalLocal, "insert(any, local CodecFactory) -> MARSHAL (write_Object called with a local object)");
        orb.destroy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
