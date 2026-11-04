/*
 * @test
 * @summary javax.naming.ldap.StartTlsRequest conformance: white-box structure + black-box behavior.
 * @run main StartTlsRequestTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.ldap.*;

public class StartTlsRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.StartTlsRequest");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(ExtendedRequest.class.isAssignableFrom(c), "implements ExtendedRequest");
        check("1.3.6.1.4.1.1466.20037".equals(StartTlsRequest.OID), "OID value");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 0), "no-arg ctor");
        for (String m : new String[]{"getID", "getEncodedValue", "createExtendedResponse"}) {
            boolean found = false;
            for (Method mm : c.getMethods()) if (mm.getName().equals(m)) found = true;
            check(found, m + " present");
        }
    }

    static void testBlackBox() throws Exception {
        StartTlsRequest r = new StartTlsRequest();
        check(StartTlsRequest.OID.equals(r.getID()), "getID");
        check(r.getEncodedValue() == null, "getEncodedValue null");

        ExtendedResponse resp = r.createExtendedResponse(null, null, 0, 0);
        check(resp != null, "createExtendedResponse not null");
        check(resp instanceof StartTlsResponse, "response is StartTlsResponse");
        check(StartTlsResponse.OID.equals(resp.getID()), "response getID");
        check(resp.getEncodedValue() == null, "response getEncodedValue null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
