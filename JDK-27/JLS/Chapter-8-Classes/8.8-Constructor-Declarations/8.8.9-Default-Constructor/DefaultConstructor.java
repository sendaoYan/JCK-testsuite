/*
 * @test
 * @summary JLS 8.8.9: if a class contains no constructor declarations, a default
 *          constructor with no formal parameters is implicitly declared.
 * @run main DefaultConstructor
 */

public class DefaultConstructor {
    int x, y;   // no constructor declared -> default constructor

    public static void main(String[] args) {
        DefaultConstructor p = new DefaultConstructor();
        if (p.x != 0 || p.y != 0) throw new AssertionError("field defaults");
        System.out.println("PASS");
    }
}
