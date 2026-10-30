/*
 * @test
 * @summary JLS 8.9.1: it is a compile-time error for the class body of an enum
 *          constant to declare an abstract method.
 * @compile/fail EnumConstantBodyAbstractMethod.java
 */

enum Operation {
    PLUS { abstract double eval(double x, double y); };   // error: abstract method
}
