/*
 * @test
 * @summary JLS 14.4 / 6.4: a local variable or parameter may shadow a field; the
 *          field remains accessible through a qualified reference.
 * @run main LocalVariableShadowing
 */

public class LocalVariableShadowing {
    int x = 10;

    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    int setX(int x) {          // parameter shadows the field
        this.x = x;
        return this.x;
    }

    public static void main(String[] args) {
        LocalVariableShadowing t = new LocalVariableShadowing();
        int x = 20;            // local variable shadows the field
        check(x == 20, "local x shadows field x");
        check(t.x == 10, "field still reachable via receiver");

        check(t.setX(9) == 9, "parameter shadows field");
        check(t.x == 9, "this.x writes the field");

        // a nested block may shadow a field with a fresh local.
        {
            int x2 = 30;
            check(x2 == 30, "nested block local");
        }
        check(t.x == 9, "field unchanged by nested local");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
