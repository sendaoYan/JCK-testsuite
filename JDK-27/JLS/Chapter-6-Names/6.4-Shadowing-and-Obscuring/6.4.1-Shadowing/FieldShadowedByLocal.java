/*
 * @test
 * @summary JLS 6.4.1 Example 6.4.1-1: a local variable shadows a field; the
 *          field remains accessible via a qualified name (C.x) or this.x.
 * @run main FieldShadowedByLocal
 */

public class FieldShadowedByLocal {
    static int x = 1;
    int y = 2;
    public static void main(String[] args) {
        int x = 0;                          // shadows the class variable x
        if (x != 0) throw new AssertionError("local x");
        if (FieldShadowedByLocal.x != 1) throw new AssertionError("qualified x");

        FieldShadowedByLocal t = new FieldShadowedByLocal();
        int y = 9;                          // shadows the instance field y
        if (y != 9) throw new AssertionError("local y");
        if (t.y != 2) throw new AssertionError("qualified instance y");

        System.out.println("PASS");
    }
}
