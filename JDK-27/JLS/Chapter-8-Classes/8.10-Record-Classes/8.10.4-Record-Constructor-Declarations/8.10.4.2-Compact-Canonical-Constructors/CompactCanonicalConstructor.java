/*
 * @test
 * @summary JLS 8.10.4.2: a compact canonical constructor declares the canonical
 *          constructor without repeating the record components; after its body
 *          completes, the component fields are initialized to the formal
 *          parameter values.
 * @run main CompactCanonicalConstructor
 */

public record CompactCanonicalConstructor(int num, int denom) {
    private static int gcd(int a, int b) {
        if (b == 0) return Math.abs(a);
        else return gcd(b, a % b);
    }
    public CompactCanonicalConstructor {
        int g = gcd(num, denom);
        num /= g;
        denom /= g;
    }

    public static void main(String[] args) {
        CompactCanonicalConstructor r = new CompactCanonicalConstructor(2, 4);
        if (r.num() != 1 || r.denom() != 2) throw new AssertionError("not reduced");
        System.out.println("PASS");
    }
}
