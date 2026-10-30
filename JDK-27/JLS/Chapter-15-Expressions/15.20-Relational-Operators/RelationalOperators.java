/*
 * @test
 * @summary JLS 15.20: numerical relational operators (with NaN comparing false)
 *          and the instanceof operator (including type patterns).
 * @run main RelationalOperators
 */

public class RelationalOperators {
    public static void main(String[] args) {
        if (!(1 < 2) || !(2 <= 2) || !(2 > 1) || !(2 >= 2))
            throw new AssertionError("numeric relational");

        double nan = Double.NaN;
        if (nan < 0 || nan >= 0 || nan <= 0 || nan > 0)
            throw new AssertionError("NaN comparisons must be false");

        Object o = "s";
        if (!(o instanceof String)) throw new AssertionError("instanceof String");
        if (o instanceof Integer) throw new AssertionError("instanceof Integer");

        if (!(o instanceof String s && s.length() == 1))
            throw new AssertionError("instanceof with type pattern");

        System.out.println("PASS");
    }
}
