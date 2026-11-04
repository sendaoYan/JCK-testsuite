/*
 * @test
 * @summary javax.xml.ws.wsaddressing.W3CEndpointReference conformance: endpoint reference.
 * @run main W3CEndpointReferenceTest
 */


public class W3CEndpointReferenceTest {
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
        javax.xml.ws.wsaddressing.W3CEndpointReference epr = b.build();
        check(epr != null, "epr non-null");
        check(epr instanceof javax.xml.ws.EndpointReference, "extends EndpointReference");
        javax.xml.transform.stream.StreamResult out = new javax.xml.transform.stream.StreamResult(new java.io.StringWriter());
        epr.writeTo(out);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
