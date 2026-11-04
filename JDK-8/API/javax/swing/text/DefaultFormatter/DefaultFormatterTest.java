/*
 * @test
 * @summary javax.swing.text.DefaultFormatter conformance: formatter (headless-safe).
 * @run main DefaultFormatterTest
 */

import javax.swing.text.DefaultFormatter;

public class DefaultFormatterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultFormatter f = new DefaultFormatter();
        check(f.getValueClass() == null, "no value class");
        f.setValueClass(String.class);
        check(f.getValueClass() == String.class, "value class");
        check(f.getOverwriteMode(), "overwrite mode");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
