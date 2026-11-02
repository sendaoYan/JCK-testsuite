/*
 * @test
 * @summary javax.print.SimpleDoc conformance: doc implementation.
 * @run main SimpleDocTest
 */

import java.lang.reflect.*;
import javax.print.SimpleDoc;

public class SimpleDocTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] a) throws Exception {
        SimpleDoc d = new SimpleDoc("data", javax.print.DocFlavor.STRING.TEXT_PLAIN, null);
        check(javax.print.DocFlavor.STRING.TEXT_PLAIN.equals(d.getDocFlavor()), "getDocFlavor");
        check("data".equals(d.getPrintData()), "getPrintData");
        check(d.getAttributes() == null, "getAttributes null");
        java.io.Reader r = d.getReaderForText();
        check(r != null, "getReaderForText non-null");
        r.close();
        check(d.getStreamForBytes() == null, "getStreamForBytes null for STRING flavor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
