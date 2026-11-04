/*
 * @test
 * @summary javax.swing.InputMap conformance: key bindings (headless-safe).
 * @run main InputMapTest
 */

import javax.swing.InputMap;
import javax.swing.KeyStroke;

public class InputMapTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void test() throws Exception {
        InputMap m = new InputMap();
        check(m.size() == 0, "empty");
        m.put(KeyStroke.getKeyStroke('a'), "action");
        check("action".equals(m.get(KeyStroke.getKeyStroke('a'))), "put/get");
    }

    public static void main(String[] args) throws Exception {
        test();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
