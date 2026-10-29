/*
 * @test
 * @summary JLS 5.1.6.1: a non-final class D that does not implement interface J
 *          is NOT disjoint from J (a subclass could implement J), so a
 *          narrowing reference conversion from D to J exists.
 * @run main AllowedNarrowingReferenceConversion
 */

interface J {}
class D {}
class E extends D implements J {}

public class AllowedNarrowingReferenceConversion {
    public static void main(String[] args) {
        D d = new E();
        // D -> J is allowed: a subclass E of D may implement J.
        J j = (J) d;
        if (!(j instanceof E)) throw new AssertionError("D -> J allowed (non-disjoint)");

        System.out.println("PASS");
    }
}
