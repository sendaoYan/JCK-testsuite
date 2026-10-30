/*
 * @test
 * @summary JLS 6.4 (Java SE 8): a parameter or local variable may shadow a field
 *          with the same name; the field remains reachable as this.name.
 * @run main ShadowingTest
 */

public class ShadowingTest {
    int x = 1;   // field

    void m(int x) {                 // parameter shadows the field
        if (this.x != 1 || x != 2) throw new AssertionError("parameter shadows field");
    }

    void n() {
        int x = 3;                  // local shadows the field
        if (this.x != 1 || x != 3) throw new AssertionError("local shadows field");
    }

    public static void main(String[] args) {
        ShadowingTest t = new ShadowingTest();
        t.m(2);
        t.n();
        System.out.println("PASS");
    }
}
