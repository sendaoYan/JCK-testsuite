/*
 * @test
 * @summary JLS 8.3.1.1: a field declaration in a class hides any accessible field
 *          of the same name in a superclass; the hidden static field can be
 *          accessed with super.x or Point.x.
 * @run main HidingClassVariable
 */

class Point {
    static int x = 2;
}

public class HidingClassVariable extends Point {
    static double x = 4.7;

    void printX() {
        double mine = x;
        int hidden = super.x;
        if (mine != 4.7) throw new AssertionError("Test.x == " + mine);
        if (hidden != 2) throw new AssertionError("Point.x == " + hidden);
    }

    public static void main(String[] args) {
        new HidingClassVariable().printX();
        System.out.println("PASS");
    }
}
