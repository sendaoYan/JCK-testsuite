/*
 * @test
 * @summary JLS 8.3.1.1: a field hides a superclass field; each instance contains
 *          both fields, and a field access uses the type of the reference
 *          expression to choose which one.
 * @run main HidingInstanceVariable
 */

class Point {
    int x = 2;
}

public class HidingInstanceVariable extends Point {
    double x = 4.7;

    void printBoth() {
        if (x != 4.7) throw new AssertionError("Test.x");
        if (super.x != 2) throw new AssertionError("Point.x via super");
    }

    public static void main(String[] args) {
        HidingInstanceVariable sample = new HidingInstanceVariable();
        sample.printBoth();
        if (sample.x != 4.7) throw new AssertionError("sample.x");
        if (((Point) sample).x != 2) throw new AssertionError("((Point)sample).x");
        System.out.println("PASS");
    }
}
