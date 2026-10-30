/*
 * @test
 * @summary JLS 8.9.2: it is a compile-time error if an enum class has an abstract
 *          method but some enum constant lacks a concrete implementation.
 * @compile/fail EnumAbstractMethodNotAllImplemented.java
 */

enum Operation {
    PLUS { double eval(double x, double y) { return x + y; } },
    MINUS;   // no class body -> does not implement eval
    abstract double eval(double x, double y);
}
