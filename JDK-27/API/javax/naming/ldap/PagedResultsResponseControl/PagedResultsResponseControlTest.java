/*
 * @test
 * @summary javax.naming.ldap.PagedResultsResponseControl conformance: white-box structure + black-box behavior.
 * @run main PagedResultsResponseControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import java.util.Arrays;

public class PagedResultsResponseControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.PagedResultsResponseControl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(BasicControl.class == c.getSuperclass(), "extends BasicControl");
        check("1.2.840.113556.1.4.319".equals(PagedResultsResponseControl.OID), "OID value");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 3 && x.getParameterTypes()[0] == String.class && x.getParameterTypes()[1] == boolean.class && x.getParameterTypes()[2] == byte[].class), "ctor(String,boolean,byte[])");
        for (String m : new String[]{"getResultSize", "getCookie"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() throws Exception {
        // BER SEQUENCE { INTEGER 42, OCTET STRING {1,2,3} }
        byte[] ber = {0x30, 0x08, 0x02, 0x01, 0x2a, 0x04, 0x03, 0x01, 0x02, 0x03};
        PagedResultsResponseControl c = new PagedResultsResponseControl(PagedResultsResponseControl.OID, true, ber);
        check(PagedResultsResponseControl.OID.equals(c.getID()), "getID");
        check(c.isCritical(), "critical");
        check(c.getResultSize() == 42, "result size");
        check(c.getCookie() != null && Arrays.equals(c.getCookie(), new byte[]{1, 2, 3}), "cookie");

        // empty cookie decodes to null
        byte[] berEmpty = {0x30, 0x05, 0x02, 0x01, 0x07, 0x04, 0x00};
        PagedResultsResponseControl c2 = new PagedResultsResponseControl("x", false, berEmpty);
        check(c2.getResultSize() == 7, "empty-cookie size");
        check(c2.getCookie() == null, "empty cookie -> null");
        check(!c2.isCritical(), "non-critical");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
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
