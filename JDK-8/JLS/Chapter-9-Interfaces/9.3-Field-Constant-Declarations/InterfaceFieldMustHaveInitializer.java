/*
 * @test
 * @summary JLS 9.3 (Java SE 8): every field in an interface is implicitly final, so it
 *          must have an initializer.
 * @compile/fail InterfaceFieldMustHaveInitializer.java
 */

interface InterfaceFieldMustHaveInitializer {
    int X;   // error: = expected
}
