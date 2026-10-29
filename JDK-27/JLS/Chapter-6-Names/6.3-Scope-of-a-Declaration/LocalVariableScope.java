/*
 * @test
 * @summary JLS 6.3 Example 6.3-2/6.3-3: a local variable's scope starts with its
 *          own initializer, so x=(x=2)*2 is valid (x is definitely assigned
 *          first) and yields 4; a later declarator may refer to an earlier one.
 * @run main LocalVariableScope
 */

public class LocalVariableScope {
    static int x = 100;   // shadowed by the local x declared in main
    public static void main(String[] args) {
        int x = (x = 2) * 2;            // local x assigned 2, then 2 * 2
        if (x != 4) throw new AssertionError("(x=2)*2 != 4");

        int two = 2, three = two + 1;   // later declarator refers to earlier one
        if (three != 3) throw new AssertionError("three != 3");

        System.out.println("PASS");
    }
}
