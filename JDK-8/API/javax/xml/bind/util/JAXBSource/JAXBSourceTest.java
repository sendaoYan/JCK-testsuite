/*
 * @test
 * @summary javax.xml.bind.util.JAXBSource conformance: extends SAXSource.
 * @run main JAXBSourceTest
 */


public class JAXBSourceTest {
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
        check(javax.xml.bind.util.JAXBSource.class.getSuperclass().getName().equals("javax.xml.transform.sax.SAXSource"), "extends SAXSource");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
