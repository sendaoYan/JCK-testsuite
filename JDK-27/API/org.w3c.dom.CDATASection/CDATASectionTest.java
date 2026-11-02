/*
 * @test
 * @summary org.w3c.dom.CDATASection conformance: interface extends Text.
 * @run main CDATASectionTest
 */

import org.w3c.dom.CDATASection;
import org.w3c.dom.Text;

public class CDATASectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(CDATASection.class.isInterface(), "CDATASection is an interface");
        check(Text.class.isAssignableFrom(CDATASection.class), "extends Text");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
