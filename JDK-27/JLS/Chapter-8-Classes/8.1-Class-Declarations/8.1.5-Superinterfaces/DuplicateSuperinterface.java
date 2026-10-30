/*
 * @test
 * @summary JLS 8.1.5: it is a compile-time error if the same interface is named
 *          by a direct superinterface type more than once in a single implements
 *          clause, even if named in different ways.
 * @compile/fail DuplicateSuperinterface.java
 */

class DuplicateSuperinterface implements java.lang.Cloneable, Cloneable {
    int x;
}
