/*
 * @test
 * @summary javax.naming.ldap.PagedResultsControl conformance: white-box structure + black-box behavior.
 * @run main PagedResultsControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;
import java.util.Arrays;

public class PagedResultsControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.PagedResultsControl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(BasicControl.class == c.getSuperclass(), "extends BasicControl");
        check("1.2.840.113556.1.4.319".equals(PagedResultsControl.OID), "OID value");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 2 && x.getParameterTypes()[0] == int.class && x.getParameterTypes()[1] == boolean.class), "ctor(int,boolean)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 3 && x.getParameterTypes()[0] == int.class && x.getParameterTypes()[1] == byte[].class && x.getParameterTypes()[2] == boolean.class), "ctor(int,byte[],boolean)");
    }

    static void testBlackBox() throws Exception {
        PagedResultsControl c = new PagedResultsControl(10, true);
        check(PagedResultsControl.OID.equals(c.getID()), "getID");
        check(c.isCritical(), "critical");
        check(c.getEncodedValue() != null, "encoded not null");
        check(c.getEncodedValue().length > 0, "encoded non-empty");

        // round-trip: request value decodes as a response with the same size
        PagedResultsResponseControl resp = new PagedResultsResponseControl(PagedResultsResponseControl.OID, true, c.getEncodedValue());
        check(resp.getResultSize() == 10, "round-trip result size");

        // with a cookie
        PagedResultsControl c2 = new PagedResultsControl(20, new byte[]{9, 8, 7}, false);
        check(!c2.isCritical(), "non-critical");
        PagedResultsResponseControl resp2 = new PagedResultsResponseControl(PagedResultsResponseControl.OID, false, c2.getEncodedValue());
        check(resp2.getResultSize() == 20, "round-trip size with cookie");
        check(resp2.getCookie() != null && Arrays.equals(resp2.getCookie(), new byte[]{9, 8, 7}), "round-trip cookie");

        // null cookie is normalized to an empty cookie
        PagedResultsControl c3 = new PagedResultsControl(7, null, true);
        PagedResultsResponseControl resp3 = new PagedResultsResponseControl(PagedResultsResponseControl.OID, true, c3.getEncodedValue());
        check(resp3.getResultSize() == 7, "null cookie size");
        check(resp3.getCookie() == null, "null cookie round-trips to null");
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
