/*
 * @test
 * @summary javax.naming.ldap.PagedResultsResponseControl conformance: white-box structure + black-box behavior.
 * @run main PagedResultsResponseControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
