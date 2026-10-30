/*
 * @test
 * @summary JLS 8.3.1.2: blank final class and instance variables may be assigned
 *          exactly once (in a static initializer / in each constructor).
 * @run main BlankFinalAssigned
 */

public class BlankFinalAssigned {
    static final int CLASS_FINAL;
    static { CLASS_FINAL = 10; }

    final int instanceFinal;

    BlankFinalAssigned() { instanceFinal = 20; }

    public static void main(String[] args) {
        BlankFinalAssigned b = new BlankFinalAssigned();
        if (CLASS_FINAL != 10) throw new AssertionError("class final");
        if (b.instanceFinal != 20) throw new AssertionError("instance final");
        System.out.println("PASS");
    }
}
