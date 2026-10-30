/*
 * @test
 * @summary JLS 8.4.8.3: an overriding method may not throw checked exceptions
 *          that are not assignable to those declared by the overridden method.
 * @compile/fail OverrideThrowsMoreChecked.java
 */

class BadPointException extends Exception {}

class Point {
    void move(int dx, int dy) {}
}

class CheckedPoint extends Point {
    void move(int dx, int dy) throws BadPointException {}   // error
}
