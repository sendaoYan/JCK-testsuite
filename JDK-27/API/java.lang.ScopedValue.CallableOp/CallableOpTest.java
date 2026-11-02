/*
 * @test
 * @summary java.lang.ScopedValue.CallableOp conformance: functional interface call().
 * @run main CallableOpTest
 */
import java.lang.ScopedValue;

public class CallableOpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        check(ScopedValue.CallableOp.class.isInterface(), "is interface");
        ScopedValue.CallableOp<String, RuntimeException> op = () -> "x";
        check("x".equals(op.call()), "call");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
