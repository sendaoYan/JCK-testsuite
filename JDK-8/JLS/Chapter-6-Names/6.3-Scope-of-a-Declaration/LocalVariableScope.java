/*
 * @test
 * @summary JLS 6.3 Example 6.3-2/6.3-3 (Java SE 8): a local variable's scope
 *          starts with its own initializer, so x=(x=2)*2 yields 4; later
 *          declarators may refer to earlier ones; the local shadows the field.
 * @run main LocalVariableScope
 */

public class LocalVariableScope {
    static int x = 100;
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    public static void main(String[] args) {
        int x = (x = 2) * 2;            // local x assigned 2, then 2 * 2
        check(x == 4, "(x=2)*2 == 4");

        int two = 2, three = two + 1;   // later declarator refers to earlier one
        check(three == 3, "later declarator refers to earlier one");

        check(LocalVariableScope.x == 100, "field still reachable via qualified name");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
