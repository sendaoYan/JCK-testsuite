/*
 * @test
 * @summary javax.xml.transform.TransformerFactory conformance: transformer factory identity transform.
 * @run main TransformerFactoryTest
 */


public class TransformerFactoryTest {
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
        javax.xml.transform.TransformerFactory tf = javax.xml.transform.TransformerFactory.newInstance();
        check(tf != null, "factory non-null");
        javax.xml.transform.Transformer t = tf.newTransformer();
        check(t != null, "transformer non-null");
        javax.xml.transform.stream.StreamSource src = new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<a/>"));
        javax.xml.transform.stream.StreamResult res = new javax.xml.transform.stream.StreamResult(new java.io.StringWriter());
        t.transform(src, res);
        check(res.getWriter().toString().indexOf("<a") >= 0, "transformed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
