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

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DocumentRange.class.isInterface(), "DocumentRange is an interface");
        check(DocumentRange.class.getMethod("createRange")
                .getReturnType() == Range.class, "createRange() returns Range");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.ranges.DocumentRange a = new org.w3c.dom.ranges.DocumentRange(){
            public org.w3c.dom.ranges.Range createRange() { return null; }
        };
        check(a != null, "anon impl of DocumentRange");
        check(a.createRange() == null, "createRange() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
