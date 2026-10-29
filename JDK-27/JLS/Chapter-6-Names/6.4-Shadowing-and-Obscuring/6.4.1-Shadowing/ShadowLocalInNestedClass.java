/*
 * @test
 * @summary JLS 6.4 Example 6.4-1 Test2: a local variable may be redeclared in a
 *          nested class declaration contained in its scope.
 * @run main ShadowLocalInNestedClass
 */

public class ShadowLocalInNestedClass {
    public static void main(String[] args) {
        int i = 100;
        class Local {
            int sum() {
                int total = 0;
                for (int i = 0; i < 3; i++) total += i;   // shadows the outer i
                return total;
            }
        }
        if (new Local().sum() != 3) throw new AssertionError("nested shadow");
        if (i != 100) throw new AssertionError("outer i unchanged");

        System.out.println("PASS");
    }
}
