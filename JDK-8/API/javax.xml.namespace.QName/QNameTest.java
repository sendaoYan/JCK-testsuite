/*
 * @test
 * @summary javax.xml.namespace.QName conformance: QName value behavior.
 * @run main QNameTest
 */


public class QNameTest {
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
        javax.xml.namespace.QName q = new javax.xml.namespace.QName("http://ns", "local", "p");
        check("http://ns".equals(q.getNamespaceURI()), "namespace uri");
        check("local".equals(q.getLocalPart()), "local part");
        check("p".equals(q.getPrefix()), "prefix");
        check("{http://ns}local".equals(q.toString()), "toString");
        check(q.equals(new javax.xml.namespace.QName("http://ns", "local", "p")), "equals");
        javax.xml.namespace.QName q2 = javax.xml.namespace.QName.valueOf("{http://ns}local");
        check(q.equals(q2), "valueOf");
        check(q.hashCode() == q2.hashCode(), "hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
