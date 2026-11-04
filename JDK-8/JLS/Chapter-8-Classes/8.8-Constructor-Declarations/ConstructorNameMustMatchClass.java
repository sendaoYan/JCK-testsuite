/*
 * @test
 * @summary JLS 8.8 (Java SE 8): a constructor declaration must have the same name as
 *          the class it is declared in; a different name makes it a method, which
 *          requires a return type.
 * @compile/fail ConstructorNameMustMatchClass.java
 */

class ConstructorNameMustMatchClass {
    NotTheClassName() {}   // error: invalid method declaration; return type required
}
