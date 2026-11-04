/*
 * @test
 * @summary javax.xml.soap.SOAPFactory conformance: SOAP factory.
 * @run main SOAPFactoryTest
 */


public class SOAPFactoryTest {
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
        javax.xml.soap.SOAPFactory sf = javax.xml.soap.SOAPFactory.newInstance();
        check(sf != null, "factory non-null");
        javax.xml.soap.Name n = sf.createName("local", "p", "urn:test");
        check("local".equals(n.getLocalName()), "local name");
        check("urn:test".equals(n.getURI()), "uri");
        check("p".equals(n.getPrefix()), "prefix");
        javax.xml.soap.SOAPElement e = sf.createElement("foo");
        check(e != null, "element non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
