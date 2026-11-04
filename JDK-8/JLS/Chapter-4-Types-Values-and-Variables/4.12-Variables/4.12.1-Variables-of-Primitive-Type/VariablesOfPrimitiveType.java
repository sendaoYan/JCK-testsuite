/*
 * @test
 * @summary JLS 4.12.1 (Java SE 8): a variable of a primitive type always holds a
 *          primitive value of that exact primitive type. Assignments are checked
 *          for assignment compatibility.
 * @run main VariablesOfPrimitiveType
 */

public class VariablesOfPrimitiveType {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        byte b = 1;
        short s = 2;
        int i = 3;
        long l = 4L;
        float f = 5.0f;
        double d = 6.0;
        char c = 'A';
        boolean z = true;

        check(b == 1 && s == 2 && i == 3 && l == 4L, "integral values hold their exact values");
        check(f == 5.0f && d == 6.0, "floating-point values hold their exact values");
        check(c == 'A', "char value");
        check(z, "boolean value");

        // A primitive variable stores the value itself (no sharing).
        int a = 10, b2 = a;
        b2 = 20;
        check(a == 10, "primitive variable is an independent copy");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
