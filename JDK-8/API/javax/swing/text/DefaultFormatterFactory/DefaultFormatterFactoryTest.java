/*
 * @test
 * @summary javax.swing.text.DefaultFormatterFactory conformance: formatter factory (headless-safe).
 * @run main DefaultFormatterFactoryTest
 */

import javax.swing.text.DefaultFormatterFactory;

public class DefaultFormatterFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        DefaultFormatterFactory f = new DefaultFormatterFactory();
        check(f.getDefaultFormatter() == null, "no formatter");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
