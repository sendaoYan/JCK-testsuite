/*
 * @test
 * @summary JLS 6.1: a class literal must use a TypeName, not a parameterized
 *          type. Box<String>.class is a compile-time error because a class
 *          literal cannot be formed from a parameterized type.
 * @compile/fail ParameterizedClassLiteral.java
 */

class Box<T> { T value; }

class ParameterizedClassLiteral {
    void m() {
        Class<?> c = Box<String>.class;   // error: parameterized type in class literal
    }
}
