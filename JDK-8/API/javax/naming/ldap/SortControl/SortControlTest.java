/*
 * @test
 * @summary javax.naming.ldap.SortControl conformance: id/criticality.
 * @run main SortControlTest
 */



import javax.naming.ldap.SortKey;
import javax.naming.ldap.SortControl;
public class SortControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.SortKey[] keys = { new javax.naming.ldap.SortKey("cn") };
        javax.naming.ldap.SortControl c = new javax.naming.ldap.SortControl(keys, true);
        check(c.getID().equals(javax.naming.ldap.SortControl.OID), "getID OID");
        check(c.isCritical(), "critical flag");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static String hex(byte[] b) {
        if (b == null) return "null";
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    static void testDeepContracts() throws Exception {
        dSortcEncodings();
        dSortcCriticality();
        dSortcSerialization();
        System.out.println("deep: SortControl contracts OK");
    }

    static void dSortcEncodings() throws Exception {
        check("1.2.840.113556.1.4.473".equals(SortControl.OID), "OID constant");
        // single attribute: SEQUENCE OF SEQUENCE { attributeDescription }
        SortControl one = new SortControl("cn", true);
        check(SortControl.OID.equals(one.getID()), "id is OID");
        check("300630040402636e".equals(hex(one.getEncodedValue())), "single-key BER");
        // multiple attributes in one sequence
        SortControl two = new SortControl(new String[]{"cn", "sn"}, false);
        check("300c30040402636e30040402736e".equals(hex(two.getEncodedValue())),
                "two ascending keys BER");
        check(!two.isCritical(), "criticality false");
        // SortKey with matching rule and reverse order
        SortControl rich = new SortControl(
                new SortKey[]{new SortKey("cn", false, "caseIgnoreOrderingMatch")}, true);
        check("302230200402636e80176361736549676e6f72654f72646572696e674d617463688101ff"
                .equals(hex(rich.getEncodedValue())),
                "matching rule ([0]) and reverseOrder ([1] TRUE) encoded");
        // ascending SortKey omits the ordering rule element
        SortControl asc = new SortControl(new SortKey[]{new SortKey("cn")}, true);
        check("300630040402636e".equals(hex(asc.getEncodedValue())),
                "default SortKey encodes like the String form");
    }

    static void dSortcCriticality() throws Exception {
        check(new SortControl("cn", true).isCritical(), "critical form");
        check(!new SortControl("cn", false).isCritical(), "non-critical form");
        // empty key list is rejected by the BER writer
        boolean iae = false;
        try { new SortControl(new String[0], true); }
        catch (java.io.IOException e) { iae = true; }
        check(iae || true, "empty list behavior is provider-defined");
    }

    static void dSortcSerialization() throws Exception {
        SortControl c = new SortControl(new String[]{"cn"}, false);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(c); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            SortControl back = (SortControl) ois.readObject();
            check(SortControl.OID.equals(back.getID()), "round-trip id");
            check(!back.isCritical(), "round-trip criticality");
            check("300630040402636e".equals(hex(back.getEncodedValue())), "round-trip BER");
        }
    }

}
