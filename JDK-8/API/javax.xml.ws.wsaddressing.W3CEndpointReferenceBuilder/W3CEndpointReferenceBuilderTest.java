/*
 * @test
 * @summary javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder conformance: endpoint reference builder.
 * @run main W3CEndpointReferenceBuilderTest
 */


public class W3CEndpointReferenceBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder b = new javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder();
        b.address("http://example.com/service");
        b.serviceName(new javax.xml.namespace.QName("urn:test", "S"));
        b.endpointName(new javax.xml.namespace.QName("urn:test", "P"));
        javax.xml.ws.wsaddressing.W3CEndpointReference epr = b.build();
        check(epr != null, "built epr non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
