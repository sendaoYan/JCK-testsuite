/*
 * @test
 * @summary org.omg.CosNaming.IstringHelper conformance: helper static id/type/narrow.
 * @run main IstringHelperTest
 */

public class IstringHelperTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(org.omg.CosNaming.IstringHelper.id() != null, "id() non-null");
        check(org.omg.CosNaming.IstringHelper.type() != null, "type() non-null");
        // ---- deepened (batch C, jdk8u372 probed): alias of string ----
        final org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init(new String[0], null);
        check(org.omg.CosNaming.IstringHelper.id().equals("IDL:omg.org/CosNaming/Istring:1.0"), "id() exact");
        org.omg.CORBA.TypeCode isTc = org.omg.CosNaming.IstringHelper.type();
        check(isTc.kind() == org.omg.CORBA.TCKind.tk_alias, "type().kind() == tk_alias (typedef Istring)");
        check(isTc.name().equals("Istring"), "alias name Istring");
        check(isTc.content_type().kind() == org.omg.CORBA.TCKind.tk_string, "content type tk_string");
        org.omg.CORBA.Any anyS = orb.create_any();
        org.omg.CosNaming.IstringHelper.insert(anyS, "hello");
        check(anyS.type().kind() == org.omg.CORBA.TCKind.tk_alias, "Any kind tk_alias");
        check(org.omg.CosNaming.IstringHelper.extract(anyS).equals("hello"), "Any round-trip");
        org.omg.CORBA.portable.OutputStream osS = orb.create_output_stream();
        org.omg.CosNaming.IstringHelper.write(osS, "cdr-value");
        check(org.omg.CosNaming.IstringHelper.read(osS.create_input_stream()).equals("cdr-value"), "CDR round-trip");
        org.omg.CORBA.Any anyCn = orb.create_any();
        org.omg.CosNaming.IstringHelper.insert(anyCn, "\u00e9");
        check(org.omg.CosNaming.IstringHelper.extract(anyCn).equals("\u00e9"),
              "non-ASCII (Latin-1 e-acute) string Any round-trip (offline tk_string negotiates ISO-8859-1; CJK would throw DATA_CONVERSION)");
        org.omg.CORBA.portable.OutputStream osCn = orb.create_output_stream();
        org.omg.CosNaming.IstringHelper.write(osCn, "\u00e9");
        check(org.omg.CosNaming.IstringHelper.read(osCn.create_input_stream()).equals("\u00e9"), "Latin-1 CDR round-trip");
        try {
            org.omg.CORBA.Any anyCjk = orb.create_any();
            org.omg.CosNaming.IstringHelper.insert(anyCjk, "\u4e2d\u6587");
            throw new AssertionError("CJK through tk_string must throw offline");
        } catch (org.omg.CORBA.DATA_CONVERSION expected) {
            check(true, "CJK through narrow-code-set tk_string -> org.omg.CORBA.DATA_CONVERSION (negotiated codeset is ISO-8859-1 offline)");
        }
        org.omg.CORBA.Any anyEmpty = orb.create_any();
        org.omg.CosNaming.IstringHelper.insert(anyEmpty, "");
        check(org.omg.CosNaming.IstringHelper.extract(anyEmpty).isEmpty(), "empty string Any round-trip");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
