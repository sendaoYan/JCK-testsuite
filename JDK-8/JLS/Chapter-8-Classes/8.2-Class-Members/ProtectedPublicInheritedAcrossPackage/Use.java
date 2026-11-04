/*
 * @test
 * @summary JLS 8.2 (Java SE 8): members declared protected or public are inherited by
 *          subclasses declared in a different package.
 * @compile points/Point.java Use.java
 * @run main Use
 */
public class Use extends points.Point {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    void touch() { x = 10; y = 20; }
    public static void main(String[] args) {
        Use u = new Use();
        u.touch();
        check(u.x == 10, "public member inherited across package");
        check(u.y == 20, "protected member inherited across package");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
