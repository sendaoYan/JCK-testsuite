/*
 * @test
 * @summary JLS 8.9.2: an enum class may declare an abstract method if every enum
 *          constant provides a concrete implementation via its class body.
 * @run main EnumAbstractMethodAllImplemented
 */

public class EnumAbstractMethodAllImplemented {
    enum Operation {
        PLUS  { double eval(double x, double y) { return x + y; } },
        MINUS { double eval(double x, double y) { return x - y; } };
        abstract double eval(double x, double y);
    }

    public static void main(String[] args) {
        if (Operation.PLUS.eval(1.0, 2.0) != 3.0) throw new AssertionError("PLUS");
        if (Operation.MINUS.eval(5.0, 2.0) != 3.0) throw new AssertionError("MINUS");
        System.out.println("PASS");
    }
}
