/*
 * @test
 * @summary java.beans.IndexedPropertyChangeEvent conformance: index and property values.
 * @run main IndexedPropertyChangeEventTest
 */

import java.beans.*;

public class IndexedPropertyChangeEventTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        IndexedPropertyChangeEvent e = new IndexedPropertyChangeEvent(new Object(), "p", "old", "new", 3);
        check(e.getIndex() == 3, "index");
        check("p".equals(e.getPropertyName()), "property name");
        check("new".equals(e.getNewValue()), "new value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
