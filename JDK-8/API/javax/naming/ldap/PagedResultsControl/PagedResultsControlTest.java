/*
 * @test
 * @summary javax.naming.ldap.PagedResultsControl conformance: id/criticality.
 * @run main PagedResultsControlTest
 */



import javax.naming.ldap.PagedResultsControl;
public class PagedResultsControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.PagedResultsControl c = new javax.naming.ldap.PagedResultsControl(100, true);
        check(c.getID().equals(javax.naming.ldap.PagedResultsControl.OID), "getID OID");
        check(c.isCritical(), "critical flag");
        javax.naming.ldap.PagedResultsControl c2 = new javax.naming.ldap.PagedResultsControl(50, new byte[0], false);
        check(!c2.isCritical(), "critical false");        testDeepContracts();
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
        dPrcOidAndEncoding();
        dPrcCookieCtor();
        dPrcSerialization();
        System.out.println("deep: PagedResultsControl contracts OK");
    }

    static void dPrcOidAndEncoding() throws Exception {
        check("1.2.840.113556.1.4.319".equals(PagedResultsControl.OID), "OID constant");
        PagedResultsControl c = new PagedResultsControl(2, true);
        check(PagedResultsControl.OID.equals(c.getID()), "id is the OID");
        check(c.isCritical(), "criticality honored");
        // BER: SEQUENCE { INTEGER pageSize, OCTET STRING (empty cookie) }
        check("30050201020400".equals(hex(c.getEncodedValue())),
                "exact BER for page size 2, empty cookie");
        // larger page size uses multi-byte INTEGER
        PagedResultsControl big = new PagedResultsControl(300, false);
        check(!big.isCritical(), "non-critical flag");
        check(hex(big.getEncodedValue()).startsWith("30060202012c"),
                "INTEGER 300 encoded 02 01 2c in a length-6 SEQUENCE");
    }

    static void dPrcCookieCtor() throws Exception {
        PagedResultsControl c = new PagedResultsControl(300, new byte[]{1, 2}, false);
        // SEQUENCE { INTEGER 300, OCTET STRING 01 02 }
        check("30080202012c04020102".equals(hex(c.getEncodedValue())),
                "cookie rides in the OCTET STRING");
        check(PagedResultsControl.OID.equals(c.getID()), "cookie ctor keeps OID");
        // zero page size encodes as INTEGER 0
        PagedResultsControl zero = new PagedResultsControl(0, true);
        check(hex(zero.getEncodedValue()).equals("30050201000400"), "page size zero legal");
    }

    static void dPrcSerialization() throws Exception {
        PagedResultsControl c = new PagedResultsControl(7, false);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(c); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            PagedResultsControl back = (PagedResultsControl) ois.readObject();
            check(PagedResultsControl.OID.equals(back.getID()), "round-trip id");
            check(!back.isCritical(), "round-trip criticality");
            check("30050201070400".equals(hex(back.getEncodedValue())), "round-trip BER");
        }
    }

}
