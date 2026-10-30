/*
 * @test
 * @summary JLS 8.10.2: it is a compile-time error for the body of a record
 *          declaration to contain an instance initializer.
 * @compile/fail RecordCannotHaveInstanceInitializer.java
 */

record Point(int x, int y) {
    { int a = 0; }   // error: instance initializer not allowed in a record
}
