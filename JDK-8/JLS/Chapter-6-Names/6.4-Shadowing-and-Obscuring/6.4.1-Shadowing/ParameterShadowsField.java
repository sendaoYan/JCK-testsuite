/*
 * @test
 * @summary JLS 6.4.1 Example 6.4.1-1 (Java SE 8): a parameter shadows a field;
 *          the field remains reachable as this.name or TypeName.name.
 * @run main ParameterShadowsField
 */

public class ParameterShadowsField {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    int x = 1;

    int m(int x) {            // parameter shadows the field
        return x + this.x;    // parameter value + field via this
    }

    public static void main(String[] args) {
        ParameterShadowsField t = new ParameterShadowsField();
        check(t.m(10) == 11, "parameter shadows field; field via this");
        check(t.x == 1, "field value unchanged");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
