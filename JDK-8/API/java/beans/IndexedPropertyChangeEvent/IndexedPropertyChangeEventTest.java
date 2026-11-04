/*
 * @test
 * @summary java.beans.IndexedPropertyChangeEvent conformance: index.
 * @run main IndexedPropertyChangeEventTest
 */

import java.beans.*;
public class IndexedPropertyChangeEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        IndexedPropertyChangeEvent e = new IndexedPropertyChangeEvent("src", "p", "old", "new", 3);
        check(e.getIndex() == 3, "index");
        check("p".equals(e.getPropertyName()), "property name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
