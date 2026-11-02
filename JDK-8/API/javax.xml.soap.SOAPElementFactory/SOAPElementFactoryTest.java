/*
 * @test
 * @summary javax.xml.soap.SOAPElementFactory conformance: SOAP element factory.
 * @run main SOAPElementFactoryTest
 */


public class SOAPElementFactoryTest {
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
        javax.xml.soap.SOAPElementFactory f = javax.xml.soap.SOAPElementFactory.newInstance();
        check(f != null, "factory non-null");
        javax.xml.soap.SOAPElement e = f.create("foo");
        check(e != null, "element non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
