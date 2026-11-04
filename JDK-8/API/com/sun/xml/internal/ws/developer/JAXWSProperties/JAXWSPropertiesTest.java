/*
 * @test
 * @summary com.sun.xml.internal.ws.developer.JAXWSProperties conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JAXWSPropertiesTest
 */

import java.lang.reflect.Modifier;


public class JAXWSPropertiesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.developer.JAXWSProperties", false, JAXWSPropertiesTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.developer.JAXWSProperties"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("ADDRESSING_ACTION"), "field ADDRESSING_ACTION");
        check(fn.contains("ADDRESSING_FROM"), "field ADDRESSING_FROM");
        check(fn.contains("ADDRESSING_MESSAGEID"), "field ADDRESSING_MESSAGEID");
        check(fn.contains("ADDRESSING_TO"), "field ADDRESSING_TO");
        check(fn.contains("CONNECT_TIMEOUT"), "field CONNECT_TIMEOUT");
        check(fn.contains("CONTENT_NEGOTIATION_PROPERTY"), "field CONTENT_NEGOTIATION_PROPERTY");
        check(fn.contains("HOSTNAME_VERIFIER"), "field HOSTNAME_VERIFIER");
        check(fn.contains("HTTP_CLIENT_STREAMING_CHUNK_SIZE"), "field HTTP_CLIENT_STREAMING_CHUNK_SIZE");
        check(fn.contains("HTTP_EXCHANGE"), "field HTTP_EXCHANGE");
        check(fn.contains("HTTP_REQUEST_URL"), "field HTTP_REQUEST_URL");
        check(fn.contains("INBOUND_HEADER_LIST_PROPERTY"), "field INBOUND_HEADER_LIST_PROPERTY");
        check(fn.contains("MTOM_THRESHOLOD_VALUE"), "field MTOM_THRESHOLOD_VALUE");
        check(fn.contains("REQUEST_TIMEOUT"), "field REQUEST_TIMEOUT");
        check(fn.contains("REST_BINDING"), "field REST_BINDING");
        check(fn.contains("SSL_SOCKET_FACTORY"), "field SSL_SOCKET_FACTORY");
        check(fn.contains("WSENDPOINT"), "field WSENDPOINT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
