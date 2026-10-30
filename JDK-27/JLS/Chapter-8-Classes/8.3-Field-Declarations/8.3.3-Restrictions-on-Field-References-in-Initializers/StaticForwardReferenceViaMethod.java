/*
 * @test
 * @summary JLS 8.3.3: access by methods is not checked for forward references;
 *          a class variable read before its initializer runs has its default
 *          value.
 * @run main StaticForwardReferenceViaMethod
 */

class Z {
    static int peek() { return j; }
    static int i = peek();
    static int j = 1;
}

public class StaticForwardReferenceViaMethod {
    public static void main(String[] args) {
        if (Z.i != 0) throw new AssertionError("j default value before init is 0");
        System.out.println("PASS");
    }
}
