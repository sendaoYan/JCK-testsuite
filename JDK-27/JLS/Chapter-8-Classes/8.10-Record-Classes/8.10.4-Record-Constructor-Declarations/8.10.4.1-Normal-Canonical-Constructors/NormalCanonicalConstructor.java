/*
 * @test
 * @summary JLS 8.10.4.1: a normal canonical constructor has formal parameters
 *          matching the record components and must initialize every component
 *          field.
 * @run main NormalCanonicalConstructor
 */

public record NormalCanonicalConstructor(int x, int y) {
    public NormalCanonicalConstructor(int x, int y) {
        if (x < 0 || y < 0) throw new IllegalArgumentException("negative");
        this.x = x;
        this.y = y;
    }

    public static void main(String[] args) {
        NormalCanonicalConstructor p = new NormalCanonicalConstructor(1, 2);
        if (p.x() != 1 || p.y() != 2) throw new AssertionError("fields");
        try {
            new NormalCanonicalConstructor(-1, 2);
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // canonical constructor validation ran
        }
        System.out.println("PASS");
    }
}
