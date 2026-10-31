/*
 * @test
 * @summary java.util.EventObject API conformance: getSource.
 * @run main EventObjectTest
 */

import java.util.EventObject;

public class EventObjectTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testEventObject() {
        Object src = new Object();
        EventObject e = new EventObject(src);
        check(e.getSource() == src, "getSource returns the constructor argument");
    }

    public static void main(String[] args) {
        testEventObject();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
