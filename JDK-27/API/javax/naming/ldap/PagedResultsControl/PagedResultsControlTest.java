/*
 * @test
 * @summary javax.naming.ldap.PagedResultsControl conformance: white-box structure + black-box behavior.
 * @run main PagedResultsControlTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
