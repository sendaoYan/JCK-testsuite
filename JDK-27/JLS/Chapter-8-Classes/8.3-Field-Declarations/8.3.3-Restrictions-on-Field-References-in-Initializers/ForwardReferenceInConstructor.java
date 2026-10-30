/*
 * @test
 * @summary JLS 8.3.3: a constructor may refer to a field declared later in the
 *          class, since access by methods and constructors is not subject to the
 *          forward-reference restriction.
 * @run main ForwardReferenceInConstructor
 */

class Test {
    Test() { k = 2; }
    int j = 1;
    int i = j;
    int k;
}

public class ForwardReferenceInConstructor {
    public static void main(String[] args) {
        Test t = new Test();
        if (t.k != 2) throw new AssertionError("constructor assigned k");
        if (t.i != 1) throw new AssertionError("i = j = 1");
        System.out.println("PASS");
    }
}
