/*
 * @test
 * @summary JLS 5.5/5.5.1 (Java SE 8): if S is a final class and T is an interface
 *          type, S must implement T, or a compile-time error occurs.
 * @compile/fail FinalClassCastToInterface.java
 */

interface Colorable { void setColor(int c); }

final class EndPoint {}

class FinalClassCastToInterface {
    void m() {
        EndPoint e = new EndPoint();
        Colorable c = (Colorable) e;   // error: inconvertible types (final class does not implement interface)
    }
}
