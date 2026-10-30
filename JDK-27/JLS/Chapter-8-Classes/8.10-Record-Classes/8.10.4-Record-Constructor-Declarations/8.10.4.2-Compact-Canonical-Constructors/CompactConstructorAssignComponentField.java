/*
 * @test
 * @summary JLS 8.10.4.2: the body of a compact constructor must not assign to a
 *          component field of the record class.
 * @compile/fail CompactConstructorAssignComponentField.java
 */

record Point(int x) {
    Point {
        this.x = 0;   // error: cannot assign to component field in compact constructor
    }
}
