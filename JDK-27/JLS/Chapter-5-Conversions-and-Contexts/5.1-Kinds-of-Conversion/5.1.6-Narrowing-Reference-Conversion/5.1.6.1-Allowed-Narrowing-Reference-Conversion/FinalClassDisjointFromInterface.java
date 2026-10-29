/*
 * @test
 * @summary JLS 5.1.6.1: a final class C that does not implement interface I is
 *          disjoint from I, so no narrowing reference conversion exists from C
 *          to I (Example 5.5-1, compile-time error #2).
 * @compile/fail FinalClassDisjointFromInterface.java
 */

interface Colorable { void setColor(int color); }

final class EndPoint {}

class FinalClassDisjointFromInterface {
    void m() {
        EndPoint e = new EndPoint();
        Colorable c = (Colorable) e;   // error: EndPoint is disjoint from Colorable
    }
}
