/*
 * @test
 * @summary java.beans.PropertyChangeEvent conformance: full contract.
 *          constructor + null source -> IllegalArgumentException, getters for
 *          source/propertyName/oldValue/newValue, propagationId default null
 *          and set/get round-trip (incl. null), and toString format containing
 *          property name, old/new values, propagation id and source.
 * @run main PropertyChangeEventTest
 */

import java.beans.PropertyChangeEvent;

public class PropertyChangeEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testConstructor() {
        Object src = new Object();
        PropertyChangeEvent e = new PropertyChangeEvent(src, "name", "old", "new");
        check(src == e.getSource(), "source identity");
        check("name".equals(e.getPropertyName()), "propertyName");
        check("old".equals(e.getOldValue()), "oldValue");
        check("new".equals(e.getNewValue()), "newValue");
        check(e.getPropagationId() == null, "propagationId default null");

        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() {
                    new PropertyChangeEvent(null, "p", "a", "b");
                }}, "null source -> IllegalArgumentException");

        PropertyChangeEvent nulls = new PropertyChangeEvent(src, null, null, null);
        check(nulls.getPropertyName() == null && nulls.getOldValue() == null && nulls.getNewValue() == null,
                "null name/old/new accepted");
    }

    static void testPropagationId() {
        PropertyChangeEvent e = new PropertyChangeEvent(new Object(), "p", null, null);
        e.setPropagationId(Integer.valueOf(5));
        check(Integer.valueOf(5).equals(e.getPropagationId()), "propagationId set/get");
        e.setPropagationId(null);
        check(e.getPropagationId() == null, "propagationId set null");
    }

    static void testToString() {
        PropertyChangeEvent e = new PropertyChangeEvent("src", "p", "old", "new");
        e.setPropagationId("id");
        String s = e.toString();
        check(s != null && s.contains("p"), "toString contains property name");
        check(s.contains("old") && s.contains("new"), "toString contains old/new");
        check(s.contains("id"), "toString contains propagationId");
        check(s.contains("src"), "toString contains source");
    }


    static void testStructure() {
        check(java.util.EventObject.class.isAssignableFrom(PropertyChangeEvent.class), "extends EventObject");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testConstructor();
        testPropagationId();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
