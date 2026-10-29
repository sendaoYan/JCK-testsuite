/*
 * @test
 * @summary JLS 5.1.6.3: the run-time validity check for a checked (or partially
 *          unchecked) narrowing reference conversion: null is always allowed;
 *          otherwise the object's class R must be the same class as T (or a
 *          subclass), or implement T (if T is an interface), or a
 *          ClassCastException is thrown. Arrays: only Object, Serializable, or
 *          Cloneable (or a matching array type) are allowed.
 * @run main NarrowingReferenceConversionsAtRunTime
 */

interface I {}
class A {}
class B extends A implements I {}

public class NarrowingReferenceConversionsAtRunTime {
    public static void main(String[] args) {
        // null passes any narrowing reference conversion.
        Object n = null;
        String ns = (String) n;
        if (ns != null) throw new AssertionError("null passes");

        // Object -> String succeeds when R (String) == T.
        Object o = "hello";
        String s = (String) o;
        if (!s.equals("hello")) throw new AssertionError("Object -> String");

        // Object -> A: R must be same class as T or a subclass.
        Object oa = new B();
        A a = (A) oa;
        if (!(a instanceof B)) throw new AssertionError("Object -> A (subclass)");

        // Object -> I: R must implement interface I.
        I i = (I) oa;
        if (!(i instanceof B)) throw new AssertionError("Object -> I (implements)");

        // Wrong class -> ClassCastException.
        try {
            Object oi = Integer.valueOf(1);
            String bad = (String) oi;
            throw new AssertionError("expected ClassCastException");
        } catch (ClassCastException expected) {
            // expected
        }

        // Array casts: matching reference array types allowed.
        Object arr = new String[0];
        String[] sa = (String[]) arr;
        if (sa.length != 0) throw new AssertionError("String[] cast");

        // Array cast to a non-matching reference array -> ClassCastException.
        try {
            Object iarr = new int[0];
            Object[] oarr = (Object[]) iarr;   // int[] is not Object[]
            throw new AssertionError("expected ClassCastException for int[] -> Object[]");
        } catch (ClassCastException expected) {
            // expected
        }

        // An array may be cast to Object, Serializable, or Cloneable only.
        Object o2 = new int[1];
        java.io.Serializable ser = (java.io.Serializable) o2;
        Cloneable cl = (Cloneable) o2;
        if (ser == null || cl == null) throw new AssertionError("array to Serializable/Cloneable");

        System.out.println("PASS");
    }
}
