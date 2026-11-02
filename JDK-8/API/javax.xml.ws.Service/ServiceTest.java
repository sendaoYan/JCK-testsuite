/*
 * @test
 * @summary javax.xml.ws.Service conformance: service creation.
 * @run main ServiceTest
 */


public class ServiceTest {
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
        javax.xml.namespace.QName qn = new javax.xml.namespace.QName("urn:test", "HelloService");
        javax.xml.ws.Service svc = javax.xml.ws.Service.create(qn);
        check(svc.getServiceName().equals(qn), "service name");
        check(svc.getWSDLDocumentLocation() == null, "no wsdl location");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
