/*
 * @test
 * @summary JLS 6.4.1 (Java SE 8): a local variable shadows a field; the field
 *          remains reachable via this.name or TypeName.name.
 * @run main LocalShadowsField
 */

public class LocalShadowsField {
    static int passed = 0;
    static void check(boolean c, String w) { if (c) passed++; else throw new AssertionError(w); }

    int x = 1;
    static int y = 2;

    int m() {
        int x = 10;               // local shadows instance field
        return x + this.x;        // local + field via this
    }

    public static void main(String[] args) {
        LocalShadowsField t = new LocalShadowsField();
        check(t.m() == 11, "local shadows instance field; field via this");

        int y = 20;               // local shadows static field
        check(y == 20 && LocalShadowsField.y == 2, "local shadows static field; field via TypeName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
