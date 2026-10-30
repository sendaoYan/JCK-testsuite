/*
 * @test
 * @summary JLS 8.3: an ambiguously inherited field may be accessed unambiguously
 *          with super or a qualified name (super.v is SuperTest.v, Frob.v is the
 *          interface's field).
 * @run main MultiplyInheritedFieldQualified
 */

interface Frob { float v = 2.0f; }
class SuperTest { int v = 3; }

public class MultiplyInheritedFieldQualified extends SuperTest implements Frob {
    void printV() {
        double r = (super.v + Frob.v) / 2;
        if (r != 2.5) throw new AssertionError("(super.v + Frob.v)/2 == " + r);
    }

    public static void main(String[] args) {
        new MultiplyInheritedFieldQualified().printV();
        System.out.println("PASS");
    }
}
