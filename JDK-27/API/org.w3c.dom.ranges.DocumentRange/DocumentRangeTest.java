/*
 * @test
 * @summary org.w3c.dom.ranges.DocumentRange conformance: interface and createRange.
 * @run main DocumentRangeTest
 */

import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;

public class DocumentRangeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(DocumentRange.class.isInterface(), "DocumentRange is an interface");
        check(DocumentRange.class.getMethod("createRange")
                .getReturnType() == Range.class, "createRange() returns Range");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
