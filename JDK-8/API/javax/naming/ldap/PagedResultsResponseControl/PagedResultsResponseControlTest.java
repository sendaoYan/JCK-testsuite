/*
 * @test
 * @summary javax.naming.ldap.PagedResultsResponseControl conformance: result size/cookie.
 * @run main PagedResultsResponseControlTest
 */



import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
public class PagedResultsResponseControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        byte[] ber = {0x30, 0x05, 0x02, 0x01, 0x00, 0x04, 0x00};
        javax.naming.ldap.PagedResultsResponseControl c = new javax.naming.ldap.PagedResultsResponseControl(javax.naming.ldap.PagedResultsResponseControl.OID, true, ber);
        check(c.getID().equals(javax.naming.ldap.PagedResultsResponseControl.OID), "getID OID");
        check(c.getResultSize() == 0, "getResultSize");        testDeepContracts();
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
        dPrrcOid();
        dPrrcParsing();
        dPrrcNegative();
        System.out.println("deep: PagedResultsResponseControl contracts OK");
    }

    static void dPrrcOid() {
        check("1.2.840.113556.1.4.319".equals(PagedResultsResponseControl.OID),
                "response OID equals request OID");
    }

    static void dPrrcParsing() throws Exception {
        // SEQUENCE { INTEGER 0, OCTET STRING "abcd" }  (size tag is INTEGER 0x02)
        byte[] more = {0x30, 0x09, 0x02, 0x01, 0x00, 0x04, 0x04, 'a', 'b', 'c', 'd'};
        PagedResultsResponseControl c = new PagedResultsResponseControl(
                PagedResultsControl.OID, true, more);
        check(c.getResultSize() == 0, "result size 0 (unknown by RFC)");
        check(java.util.Arrays.equals(c.getCookie(), new byte[]{'a', 'b', 'c', 'd'}),
                "cookie parsed");
        check(c.isCritical(), "criticality stored");
        check(PagedResultsControl.OID.equals(c.getID()), "id stored");
        // empty OCTET STRING cookie means "no more pages" and yields null
        byte[] last = {0x30, 0x05, 0x02, 0x01, 0x00, 0x04, 0x00};
        PagedResultsResponseControl done = new PagedResultsResponseControl("x", false, last);
        check(done.getCookie() == null, "empty cookie -> null");
        check(done.getResultSize() == 0, "size 0 with empty cookie");
        // known size
        byte[] sized = {0x30, 0x09, 0x02, 0x01, 0x2a, 0x04, 0x04, 'a', 'b', 'c', 'd'};
        PagedResultsResponseControl known = new PagedResultsResponseControl("x", true, sized);
        check(known.getResultSize() == 42, "size 42 parsed");
        check(java.util.Arrays.equals(known.getCookie(), new byte[]{'a', 'b', 'c', 'd'}),
                "cookie alongside known size");
    }

    static void dPrrcNegative() {
        boolean ioe1 = false, ioe2 = false, ioe3 = false;
        try {
            new PagedResultsResponseControl("x", true, new byte[]{0x30});
        } catch (java.io.IOException e) {
            ioe1 = true;
            check(e.getMessage() != null, "truncated sequence message");
        }
        try {
            new PagedResultsResponseControl("x", true, new byte[]{0x30, 0x03, 0x02, 0x01});
        } catch (java.io.IOException e) { ioe2 = true; }
        try {
            new PagedResultsResponseControl("x", true, new byte[]{0x30, 0x02, 0x0a, 0x01});
        } catch (java.io.IOException e) { ioe3 = true; }
        check(ioe1, "empty body -> IOException");
        check(ioe2, "truncated INTEGER -> IOException");
        check(ioe3, "missing cookie -> IOException");
    }

}
