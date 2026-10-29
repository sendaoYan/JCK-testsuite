/*
 * @test
 * @summary JLS 7.6 Example 7.6-1: it is a compile-time error if the name of a
 *          top level class or interface appears as the name of any other top
 *          level class or interface in the same compilation unit.
 * @compile/fail DuplicateTopLevelName.java
 */

class Point { int x, y; }

interface Point {   // error: Point already defined
    int getR();
}
