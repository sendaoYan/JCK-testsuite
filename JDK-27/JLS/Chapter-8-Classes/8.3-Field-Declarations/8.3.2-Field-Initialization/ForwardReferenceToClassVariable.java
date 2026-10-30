/*
 * @test
 * @summary JLS 8.3.2: an instance variable initializer may refer by simple name
 *          to a class variable declared to its right (forward reference is not
 *          an error for a class variable).
 * @run main ForwardReferenceToClassVariable
 */

class Test {
    float f = j;      // refers to class variable j declared below
    static int j = 1;
}

public class ForwardReferenceToClassVariable {
    public static void main(String[] args) {
        Test t = new Test();
        if (t.f != 1.0f) throw new AssertionError("forward reference to class variable");
        System.out.println("PASS");
    }
}
