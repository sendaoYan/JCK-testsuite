/*
 * @test
 * @summary java.lang.invoke.VarHandle.AccessMode conformance: white-box + black-box behavior.
 * @run main AccessModeTest
 */

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;

public class AccessModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.VarHandle$AccessMode");
        check(c.isEnum(), "is an enum");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        check(c.getEnumConstants().length == 31, "thirty-one constants");
        check(c.getDeclaringClass() == VarHandle.class, "nested in VarHandle");
        check(c.getDeclaredMethod("methodName") != null, "methodName()");
        check(c.getDeclaredMethod("valueFromMethodName", String.class) != null,
              "valueFromMethodName(String)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        // every constant round-trips through methodName/valueFromMethodName
        boolean allRoundTrip = true;
        for (AccessMode m : AccessMode.values()) {
            if (AccessMode.valueFromMethodName(m.methodName()) != m) allRoundTrip = false;
        }
        check(allRoundTrip, "methodName <-> valueFromMethodName round trip");
        check(AccessMode.GET.methodName().equals("get"), "GET -> get");
        check(AccessMode.SET_VOLATILE.methodName().equals("setVolatile"), "SET_VOLATILE");
        check(AccessMode.COMPARE_AND_SET.methodName().equals("compareAndSet"), "COMPARE_AND_SET");
        check(AccessMode.GET_AND_ADD_ACQUIRE.methodName().equals("getAndAddAcquire"),
              "GET_AND_ADD_ACQUIRE");
        check(AccessMode.WEAK_COMPARE_AND_SET_RELEASE.methodName()
                      .equals("weakCompareAndSetRelease"), "WEAK_COMPARE_AND_SET_RELEASE");
        check(AccessMode.GET_AND_BITWISE_XOR.methodName().equals("getAndBitwiseXor"),
              "GET_AND_BITWISE_XOR");
        check(AccessMode.valueFromMethodName("get") == AccessMode.GET, "parse get");
        check(AccessMode.valueFromMethodName("setOpaque") == AccessMode.SET_OPAQUE, "parse setOpaque");
        try {
            AccessMode.valueFromMethodName("bogus");
            check(false, "unknown method name rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "valueFromMethodName IAE");
        }
        try {
            AccessMode.valueFromMethodName(null);
            check(false, "null rejected");
        } catch (NullPointerException expected) {
            check(true, "valueFromMethodName null NPE");
        }

        // valueOf round trip
        for (AccessMode m : AccessMode.values()) check(AccessMode.valueOf(m.name()) == m,
              "valueOf " + m);
        try {
            AccessMode.valueOf("NO_SUCH");
            check(false, "unknown constant rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "valueOf unknown IAE");
        }

        // the modes order a real VarHandle accepts
        VarHandle vh = MethodHandles.arrayElementVarHandle(int[].class);
        check(vh.isAccessModeSupported(AccessMode.GET), "GET supported");
        check(vh.isAccessModeSupported(AccessMode.COMPARE_AND_EXCHANGE), "CAE supported");
        check(vh.isAccessModeSupported(AccessMode.GET_AND_BITWISE_OR), "bitwise supported");
        check(vh.accessModeType(AccessMode.GET).returnType() == int.class,
              "accessModeType return");
        check(vh.accessModeType(AccessMode.SET).returnType() == void.class,
              "SET returns void");
        check(vh.toMethodHandle(AccessMode.GET_AND_ADD) != null, "toMethodHandle per mode");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
