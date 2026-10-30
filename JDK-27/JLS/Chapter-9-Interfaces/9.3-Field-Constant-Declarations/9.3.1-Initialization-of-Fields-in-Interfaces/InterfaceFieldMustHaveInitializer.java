/*
 * @test
 * @summary JLS 9.3.1: every declarator in a field declaration of an interface
 *          must have a variable initializer.
 * @compile/fail InterfaceFieldMustHaveInitializer.java
 */

interface InterfaceFieldMustHaveInitializer {
    int X;   // error: field must have an initializer
}
