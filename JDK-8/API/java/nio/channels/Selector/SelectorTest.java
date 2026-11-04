/*
 * @test
 * @summary java.nio.channels.Selector conformance: open/select/keys.
 * @run main SelectorTest
 */

import java.nio.channels.Selector;

public class SelectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSelector() throws Exception {
        Selector sel = Selector.open();
        check(sel.isOpen(), "isOpen");
        check(sel.provider() != null, "provider");
        check(sel.keys().isEmpty(), "keys empty");
        check(sel.selectedKeys().isEmpty(), "selectedKeys empty");
        check(sel.selectNow() >= 0, "selectNow");
        check(sel.toString() != null, "toString");
        sel.close();
        check(!sel.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testSelector();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
