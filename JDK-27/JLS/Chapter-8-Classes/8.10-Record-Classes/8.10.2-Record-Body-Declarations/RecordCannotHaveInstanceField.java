/*
 * @test
 * @summary JLS 8.10.2: it is a compile-time error for the body of a record
 *          declaration to contain a non-static field declaration.
 * @compile/fail RecordCannotHaveInstanceField.java
 */

record Point(int x, int y) {
    int z;   // error: non-static field not allowed in a record
}
