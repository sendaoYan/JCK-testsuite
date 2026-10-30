/*
 * @test
 * @summary JLS 15.21: equality operators for numeric, boolean and reference
 *          types; NaN is never == to anything including itself.
 * @run main EqualityOperators
 */

public class EqualityOperators {
    public static void main(String[] args) {
        if (1 == 2) throw new AssertionError("numeric ==");
        if (1 != 1) throw new AssertionError("numeric !=");

        if (true != true) throw new AssertionError("boolean ==");

        Object a = new Object();
        Object b = a;
        Object c = new Object();
        if (a != b) throw new AssertionError("reference identity ==");
        if (a == c) throw new AssertionError("distinct objects !=");
        if (null != null) throw new AssertionError("null == null");

        double nan = Double.NaN;
        if (nan == nan) throw new AssertionError("NaN == NaN");
        if (!(nan != nan)) throw new AssertionError("NaN != NaN");

        System.out.println("PASS");
    }
}
