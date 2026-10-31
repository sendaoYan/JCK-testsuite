/*
 * @test
 * @summary java.util.PropertyResourceBundle API conformance: load from properties stream.
 * @run main PropertyResourceBundleTest
 */

import java.io.ByteArrayInputStream;
import java.util.PropertyResourceBundle;

public class PropertyResourceBundleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPropertyResourceBundle() throws Exception {
        PropertyResourceBundle b = new PropertyResourceBundle(
            new ByteArrayInputStream("k=v\nn=3\n".getBytes("US-ASCII")));
        check("v".equals(b.getString("k")), "getString");
        check("3".equals(b.getString("n")), "getString for int-valued property");
        check(b.containsKey("k"), "containsKey");
    }

    public static void main(String[] args) throws Exception {
        testPropertyResourceBundle();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
