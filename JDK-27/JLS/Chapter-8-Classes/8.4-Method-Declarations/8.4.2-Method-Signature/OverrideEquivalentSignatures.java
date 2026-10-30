/*
 * @test
 * @summary JLS 8.4.2: it is a compile-time error to declare two methods with
 *          override-equivalent signatures in a class.
 * @compile/fail OverrideEquivalentSignatures.java
 */

class OverrideEquivalentSignatures {
    int x, y;
    abstract void move(int dx, int dy);
    void move(int dx, int dy) { x += dx; y += dy; }   // error: same signature
}
