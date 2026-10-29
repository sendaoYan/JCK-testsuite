/*
 * @test
 * @summary JLS 4.12: a variable is a storage location with an associated
 *          compile-time type (primitive or reference). Its value is changed by
 *          an assignment or by prefix/postfix ++ and --.
 * @run main Variables
 */

public class Variables {
    public static void main(String[] args) {
        // A variable holds a value of its declared type.
        int x = 1;

        // Assignment changes the value.
        x = 2;
        if (x != 2) throw new AssertionError("assignment changes value");

        // Prefix/postfix ++ and -- change the value.
        x++;
        if (x != 3) throw new AssertionError("postfix ++");
        ++x;
        if (x != 4) throw new AssertionError("prefix ++");
        x--;
        if (x != 3) throw new AssertionError("postfix --");
        --x;
        if (x != 2) throw new AssertionError("prefix --");

        // A reference variable's value is a reference (or null).
        Object o = new Object();
        Object p = o;
        p = null;
        if (o == null) throw new AssertionError("reference assignment is by value of pointer");

        System.out.println("PASS");
    }
}
