/*
 * @test
 * @summary java.beans.IndexedPropertyChangeEvent conformance: white-box structure + black-box behavior.
 * @run main IndexedPropertyChangeEventTest
 */

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;

public class IndexedPropertyChangeEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.IndexedPropertyChangeEvent");
        check(PropertyChangeEvent.class.isAssignableFrom(c), "extends PropertyChangeEvent");
    }

    static void testBlackBox() throws Exception {
        Object src = new Object();
        IndexedPropertyChangeEvent e = new IndexedPropertyChangeEvent(src, "items", "a", "b", 3);
        check(e.getIndex() == 3, "index");
        check("items".equals(e.getPropertyName()), "property name inherited");
        check("a".equals(e.getOldValue()) && "b".equals(e.getNewValue()), "old/new inherited");
        check(e.getSource() == src, "source inherited");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
