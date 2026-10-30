/*
 * @test
 * @summary JLS 8.3: if the same field declaration is inherited from an interface
 *          by multiple paths, it is inherited only once and may be referred to by
 *          simple name without ambiguity.
 * @run main ReInheritedFieldOnce
 */

interface Colorable { int RED = 0xff0000; }
interface Paintable extends Colorable { int MATTE = 0; }
class ColoredPoint implements Colorable {}

public class ReInheritedFieldOnce extends ColoredPoint implements Paintable {
    int p = RED;   // unambiguous despite being reachable by two paths

    public static void main(String[] args) {
        ReInheritedFieldOnce r = new ReInheritedFieldOnce();
        if (r.p != 0xff0000) throw new AssertionError("re-inherited field");
        System.out.println("PASS");
    }
}
