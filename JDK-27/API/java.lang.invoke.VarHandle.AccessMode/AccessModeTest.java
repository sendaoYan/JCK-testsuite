/*
 * @test
 * @summary java.lang.invoke.VarHandle.AccessMode conformance: enum methodName/valueFromMethodName.
 * @run main AccessModeTest
 */
import java.lang.invoke.VarHandle;

public class AccessModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        check(VarHandle.AccessMode.class.isEnum(), "is enum");
        check(VarHandle.AccessMode.values().length > 30, "many modes");
        check(VarHandle.AccessMode.GET.methodName().equals("get"), "GET methodName");
        check(VarHandle.AccessMode.valueOf("SET") == VarHandle.AccessMode.SET, "valueOf");
        check(VarHandle.AccessMode.valueFromMethodName("getAndAdd") == VarHandle.AccessMode.GET_AND_ADD, "valueFromMethodName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
