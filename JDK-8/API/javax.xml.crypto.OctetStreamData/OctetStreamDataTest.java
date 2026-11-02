/*
 * @test
 * @summary javax.xml.crypto.OctetStreamData conformance: octet stream data.
 * @run main OctetStreamDataTest
 */


public class OctetStreamDataTest {
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
        java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(new byte[]{1, 2, 3});
        javax.xml.crypto.OctetStreamData d = new javax.xml.crypto.OctetStreamData(in, "uri", "mime");
        check(d.getOctetStream() == in, "stream");
        check("uri".equals(d.getURI()), "uri");
        check("mime".equals(d.getMimeType()), "mime type");
        check(d instanceof javax.xml.crypto.Data, "is Data");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
