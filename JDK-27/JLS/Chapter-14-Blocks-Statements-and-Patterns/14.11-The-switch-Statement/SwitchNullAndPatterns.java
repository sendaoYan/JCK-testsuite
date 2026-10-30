/*
 * @test
 * @summary JLS 14.11.1: a switch label may be a case null label, a case label
 *          with a type pattern, a guarded case label, or a record pattern.
 * @run main SwitchNullAndPatterns
 */

record Pair(int a, int b) {}

public class SwitchNullAndPatterns {
    public static void main(String[] args) {
        int r = 0;

        Object o = null;
        switch (o) {
            case null -> r = 1;
            case String s -> r = 2;
            default -> r = 3;
        }
        if (r != 1) throw new AssertionError("case null: r == " + r);

        Object s = "hi";
        switch (s) {
            case String str -> r = str.length();
            default -> r = -1;
        }
        if (r != 2) throw new AssertionError("type pattern: r == " + r);

        Integer j = 42;
        switch (j) {
            case Integer i when i <= 0 -> r = 0;   // guard fails
            case Integer i -> r = i;               // unguarded applies
        }
        if (r != 42) throw new AssertionError("guarded pattern: r == " + r);

        Object p = new Pair(3, 4);
        switch (p) {
            case Pair(int a, int b) -> r = a + b;
            default -> r = -1;
        }
        if (r != 7) throw new AssertionError("record pattern: r == " + r);

        System.out.println("PASS");
    }
}
