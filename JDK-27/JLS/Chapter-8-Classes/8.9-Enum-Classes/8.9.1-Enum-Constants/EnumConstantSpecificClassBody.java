/*
 * @test
 * @summary JLS 8.9.1: an enum constant may have a class body that implicitly
 *          declares an anonymous direct subclass of the enclosing enum class.
 * @run main EnumConstantSpecificClassBody
 */

public class EnumConstantSpecificClassBody {
    enum Operation {
        PLUS       { double eval(double x, double y) { return x + y; } },
        MINUS      { double eval(double x, double y) { return x - y; } },
        TIMES      { double eval(double x, double y) { return x * y; } },
        DIVIDED_BY { double eval(double x, double y) { return x / y; } };
        abstract double eval(double x, double y);
    }

    public static void main(String[] args) {
        if (Operation.PLUS.eval(2.0, 4.0) != 6.0) throw new AssertionError("PLUS");
        if (Operation.MINUS.eval(2.0, 4.0) != -2.0) throw new AssertionError("MINUS");
        if (Operation.TIMES.eval(2.0, 4.0) != 8.0) throw new AssertionError("TIMES");
        if (Operation.DIVIDED_BY.eval(2.0, 4.0) != 0.5) throw new AssertionError("DIVIDED_BY");
        System.out.println("PASS");
    }
}
