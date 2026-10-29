/*
 * @test
 * @summary JLS 6.7: fully qualified names and canonical names. A member class
 *          O1.I has fully qualified names p.O1.I and p.O2.I, but only p.O1.I is
 *          its canonical name; local and anonymous classes have no canonical name.
 * @run main p.FullyQualifiedAndCanonical
 */
package p;

class O1 { class I {} }
class O2 extends O1 {}

public class FullyQualifiedAndCanonical {
    public static void main(String[] args) {
        // Primitive array types: canonical name mirrors the spec's notation.
        if (!"int[]".equals(int[].class.getCanonicalName()))
            throw new AssertionError("int[] canonical name");

        // Member class of O1: canonical name is p.O1.I.
        if (!"p.O1.I".equals(O1.I.class.getCanonicalName()))
            throw new AssertionError(O1.I.class.getCanonicalName());

        // Inherited member class is the same class; canonical name is p.O1.I,
        // even though p.O2.I is also a fully qualified name for it.
        if (!"p.O1.I".equals(O2.I.class.getCanonicalName()))
            throw new AssertionError(O2.I.class.getCanonicalName());

        // A local class has no canonical name (getCanonicalName() == null).
        class Local {}
        if (Local.class.getCanonicalName() != null)
            throw new AssertionError("local class has no canonical name");

        // An anonymous class has no canonical name.
        Object anon = new Object() {};
        if (anon.getClass().getCanonicalName() != null)
            throw new AssertionError("anonymous class has no canonical name");

        System.out.println("PASS");
    }
}
