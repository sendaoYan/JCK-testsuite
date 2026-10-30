/*
 * @test
 * @summary JLS 8.3: it is a compile-time error for the body of a class
 *          declaration to declare two fields with the same name.
 * @compile/fail DuplicateFieldName.java
 */

class DuplicateFieldName {
    int x;
    int x;   // error: variable x is already defined
}
