/*
 * @test
 * @summary JLS 7.6 (Java SE 8): it is a compile-time error if the name of a top
 *          level type appears as the name of any other top level type declared
 *          in the same package (Example 7.6-1).
 * @compile/fail DuplicateTopLevelName.java
 */

class Point { int x, y; }

interface Point {   // error: Point already defined
    int getR();
}
