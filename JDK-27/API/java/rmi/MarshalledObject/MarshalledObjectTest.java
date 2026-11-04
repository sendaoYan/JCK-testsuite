/*
 * @test
 * @summary java.rmi.MarshalledObject conformance: full contract.
 *          final + Serializable, constructor serializes (non-serializable ->
 *          IOException), get returns an equal-but-distinct deep copy on every
 *          call (mutation isolation), null payload, and equals/hashCode
 *          (self, content-equal, content-different, null, wrong type).
 * @run main MarshalledObjectTest
 */

import java.io.IOException;
import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.util.Arrays;

public class MarshalledObjectTest {
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

    static void testStructure() {
        Class<?> c = MarshalledObject.class;
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
    }

    static void testStringRoundTrip() throws Exception {
        MarshalledObject<String> mo = new MarshalledObject<String>("hello");
        check("hello".equals(mo.get()), "String round-trip");
        check(mo.equals(mo), "equals self");
    }

    static void testCopyIsolation() throws Exception {
        int[] arr = { 1, 2, 3 };
        MarshalledObject<int[]> mo = new MarshalledObject<int[]>(arr);
        int[] back = mo.get();
        check(back != arr, "get returns a copy, not the same array");
        check(Arrays.equals(arr, back), "int[] content round-trip");
        check(mo.get() != mo.get(), "two gets return distinct copies");
        back[0] = 99;
        int[] again = mo.get();
        check(again[0] == 1, "mutating returned copy does not affect marshalled content");
    }

    static void testNullAndNonSerializable() throws Exception {
        MarshalledObject<String> mon = new MarshalledObject<String>(null);
        check(mon.get() == null, "null payload get -> null");
        check(mon.equals(new MarshalledObject<String>(null)), "two null-payload equal");

        checkThrows(IOException.class,
                new ThrowingRunnable() { public void run() throws Exception {
                    new MarshalledObject<Object>(new Object());
                }}, "non-serializable payload -> IOException");
    }

    static void testEqualsHashCode() throws Exception {
        MarshalledObject<String> a = new MarshalledObject<String>("hello");
        MarshalledObject<String> b = new MarshalledObject<String>("hello");
        check(a.equals(b) && b.equals(a), "content-equal symmetric");
        check(a.hashCode() == b.hashCode(), "equal hash for equal content");
        check(!a.equals(new MarshalledObject<String>("world")), "different content not equal");
        check(!a.equals(null), "not equals null");
        check(!a.equals("hello"), "not equals non-MarshalledObject");
    }

    public static void main(String[] args) throws Exception {
        testStructure();
        testStringRoundTrip();
        testCopyIsolation();
        testNullAndNonSerializable();
        testEqualsHashCode();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
