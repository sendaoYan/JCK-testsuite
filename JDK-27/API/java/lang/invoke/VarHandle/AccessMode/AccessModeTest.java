/*
 * @test
 * @summary java.lang.invoke.VarHandle.AccessMode conformance: white-box + black-box behavior.
 *          Deepened: all 31 constants locked in declaration order (names + ordinals),
 *          methodName() derivation and injectivity, toString/compareTo/defensive-copy
 *          enum contracts, support counts across var-type families, accessModeType
 *          precision incl. unsupported modes, null-mode exceptions.
 * @run main AccessModeTest
 */

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;

public class AccessModeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrowsMsg(Class<? extends Throwable> expected, String msgPart,
                               ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                check(t.getMessage() != null && t.getMessage().contains(msgPart),
                        msg + ": message should contain '" + msgPart + "' but was: " + t.getMessage());
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    /** Declaration order of the enum constants, locked by javap + probe on JDK 27. */
    static final String[] ORDER = {
        "GET", "SET", "GET_VOLATILE", "SET_VOLATILE", "GET_ACQUIRE", "SET_RELEASE",
        "GET_OPAQUE", "SET_OPAQUE", "COMPARE_AND_SET", "COMPARE_AND_EXCHANGE",
        "COMPARE_AND_EXCHANGE_ACQUIRE", "COMPARE_AND_EXCHANGE_RELEASE",
        "WEAK_COMPARE_AND_SET_PLAIN", "WEAK_COMPARE_AND_SET", "WEAK_COMPARE_AND_SET_ACQUIRE",
        "WEAK_COMPARE_AND_SET_RELEASE", "GET_AND_SET", "GET_AND_SET_ACQUIRE", "GET_AND_SET_RELEASE",
        "GET_AND_ADD", "GET_AND_ADD_ACQUIRE", "GET_AND_ADD_RELEASE",
        "GET_AND_BITWISE_OR", "GET_AND_BITWISE_OR_RELEASE", "GET_AND_BITWISE_OR_ACQUIRE",
        "GET_AND_BITWISE_AND", "GET_AND_BITWISE_AND_RELEASE", "GET_AND_BITWISE_AND_ACQUIRE",
        "GET_AND_BITWISE_XOR", "GET_AND_BITWISE_XOR_RELEASE", "GET_AND_BITWISE_XOR_ACQUIRE",
    };

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

    // ---- Part 3: deepened contract coverage ----

    static void testEnumContract() {
        AccessMode[] vals = AccessMode.values();
        check(vals.length == ORDER.length, "constant count matches order table");
        boolean orderOk = true;
        for (int i = 0; i < ORDER.length; i++)
            orderOk &= vals[i].name().equals(ORDER[i]) && vals[i].ordinal() == i;
        check(orderOk, "values() order and ordinals locked to table");
        // toString is Enum's default: the constant name (AccessMode does not override it)
        boolean tsOk = true;
        for (AccessMode m : vals) tsOk &= m.toString().equals(m.name());
        check(tsOk, "toString == name for every constant");
        // compareTo follows declaration order (Enum contract)
        boolean cmpOk = true;
        for (int i = 1; i < vals.length; i++) cmpOk &= vals[i - 1].compareTo(vals[i]) < 0;
        check(cmpOk, "compareTo follows declaration order");
        check(AccessMode.GET.compareTo(AccessMode.GET) == 0, "compareTo self is zero");
        // values() returns a fresh, defensively copied array every call
        AccessMode[] mutate = AccessMode.values();
        AccessMode saved = mutate[0];
        mutate[0] = null;
        check(AccessMode.values()[0] == saved, "values() defensive copy");
        check(Enum.valueOf(AccessMode.class, "GET") == AccessMode.GET, "Enum.valueOf lookup");
    }

    static void testMethodNameMatrix() {
        // methodName() derivation: split the name on '_', first part fully lowercased,
        // each remaining part capitalized first letter + lowercased rest
        boolean allOk = true;
        java.util.HashSet<String> seen = new java.util.HashSet<>();
        for (AccessMode m : AccessMode.values()) {
            String[] parts = m.name().split("_");
            StringBuilder sb = new StringBuilder(parts[0].toLowerCase(java.util.Locale.ROOT));
            for (int i = 1; i < parts.length; i++)
                sb.append(Character.toUpperCase(parts[i].charAt(0)))
                  .append(parts[i].substring(1).toLowerCase(java.util.Locale.ROOT));
            if (!m.methodName().equals(sb.toString())) allOk = false;
            seen.add(m.methodName());
        }
        check(allOk, "methodName derivation holds for all 31 constants");
        check(seen.size() == 31, "methodNames are pairwise distinct");
        // spot locks on representative constants (measured on JDK 27)
        check(AccessMode.GET_ACQUIRE.methodName().equals("getAcquire"), "getAcquire");
        check(AccessMode.SET_RELEASE.methodName().equals("setRelease"), "setRelease");
        check(AccessMode.SET_OPAQUE.methodName().equals("setOpaque"), "setOpaque");
        check(AccessMode.COMPARE_AND_EXCHANGE_RELEASE.methodName().equals("compareAndExchangeRelease"),
                "compareAndExchangeRelease");
        check(AccessMode.WEAK_COMPARE_AND_SET_PLAIN.methodName().equals("weakCompareAndSetPlain"),
                "weakCompareAndSetPlain");
        check(AccessMode.WEAK_COMPARE_AND_SET_ACQUIRE.methodName().equals("weakCompareAndSetAcquire"),
                "weakCompareAndSetAcquire");
        check(AccessMode.GET_AND_SET_RELEASE.methodName().equals("getAndSetRelease"),
                "getAndSetRelease");
        check(AccessMode.GET_AND_ADD_RELEASE.methodName().equals("getAndAddRelease"),
                "getAndAddRelease");
        check(AccessMode.GET_AND_BITWISE_OR_RELEASE.methodName().equals("getAndBitwiseOrRelease"),
                "getAndBitwiseOrRelease");
        check(AccessMode.GET_AND_BITWISE_AND_ACQUIRE.methodName().equals("getAndBitwiseAndAcquire"),
                "getAndBitwiseAndAcquire");
        check(AccessMode.GET_AND_BITWISE_XOR_ACQUIRE.methodName().equals("getAndBitwiseXorAcquire"),
                "getAndBitwiseXorAcquire");
        checkThrowsMsg(IllegalArgumentException.class, "No AccessMode value for method name",
                () -> AccessMode.valueFromMethodName(""), "empty method name IAE message");
        checkThrowsMsg(NullPointerException.class, "is null",
                () -> AccessMode.valueFromMethodName(null), "null method name NPE");
    }

    static void testSupportMatrix() {
        VarHandle intArr = MethodHandles.arrayElementVarHandle(int[].class);
        VarHandle strArr = MethodHandles.arrayElementVarHandle(String[].class);
        VarHandle boolArr = MethodHandles.arrayElementVarHandle(boolean[].class);
        VarHandle longArr = MethodHandles.arrayElementVarHandle(long[].class);
        int nInt = 0, nStr = 0, nBool = 0, nLong = 0;
        for (AccessMode m : AccessMode.values()) {
            if (intArr.isAccessModeSupported(m)) nInt++;
            if (strArr.isAccessModeSupported(m)) nStr++;
            if (boolArr.isAccessModeSupported(m)) nBool++;
            if (longArr.isAccessModeSupported(m)) nLong++;
        }
        check(nInt == 31, "int[] supports all 31 modes");
        check(nLong == 31, "long[] supports all 31 modes");
        check(nStr == 19, "String[] supports 19 (31 - 3 add - 9 bitwise)");
        check(nBool == 28, "boolean[] supports 28 (31 - 3 add)");
        check(!strArr.isAccessModeSupported(AccessMode.GET_AND_ADD_ACQUIRE),
                "String[] add acquire false");
        check(!strArr.isAccessModeSupported(AccessMode.GET_AND_BITWISE_AND_RELEASE),
                "String[] bitwise false");
        check(!boolArr.isAccessModeSupported(AccessMode.GET_AND_ADD), "boolean[] add false");
        check(boolArr.isAccessModeSupported(AccessMode.GET_AND_BITWISE_XOR_RELEASE),
                "boolean[] bitwise true");
    }

    static void testAccessModeTypeMatrix() {
        VarHandle vh = MethodHandles.arrayElementVarHandle(int[].class);
        check(vh.accessModeType(AccessMode.GET)
                .equals(MethodType.methodType(int.class, int[].class, int.class)), "GET type");
        check(vh.accessModeType(AccessMode.GET_VOLATILE)
                .equals(MethodType.methodType(int.class, int[].class, int.class)),
                "GET_VOLATILE type");
        check(vh.accessModeType(AccessMode.SET)
                .equals(MethodType.methodType(void.class, int[].class, int.class, int.class)),
                "SET type");
        check(vh.accessModeType(AccessMode.SET_OPAQUE)
                .equals(MethodType.methodType(void.class, int[].class, int.class, int.class)),
                "SET_OPAQUE type");
        check(vh.accessModeType(AccessMode.COMPARE_AND_SET).equals(MethodType.methodType(
                boolean.class, int[].class, int.class, int.class, int.class)), "CAS type");
        check(vh.accessModeType(AccessMode.WEAK_COMPARE_AND_SET).equals(MethodType.methodType(
                boolean.class, int[].class, int.class, int.class, int.class)), "weak CAS type");
        check(vh.accessModeType(AccessMode.WEAK_COMPARE_AND_SET_PLAIN)
                      .equals(vh.accessModeType(AccessMode.WEAK_COMPARE_AND_SET_RELEASE)),
                "weak CAS family shares one type");
        check(vh.accessModeType(AccessMode.COMPARE_AND_EXCHANGE).equals(MethodType.methodType(
                int.class, int[].class, int.class, int.class, int.class)), "CAE type");
        check(vh.accessModeType(AccessMode.COMPARE_AND_EXCHANGE_ACQUIRE).equals(MethodType.methodType(
                int.class, int[].class, int.class, int.class, int.class)), "CAE acquire type");
        check(vh.accessModeType(AccessMode.GET_AND_SET_ACQUIRE)
                .equals(MethodType.methodType(int.class, int[].class, int.class, int.class)),
                "GET_AND_SET type");
        check(vh.accessModeType(AccessMode.GET_AND_ADD_RELEASE)
                .equals(MethodType.methodType(int.class, int[].class, int.class, int.class)),
                "GET_AND_ADD type");
        check(vh.accessModeType(AccessMode.GET_AND_BITWISE_AND_ACQUIRE)
                .equals(MethodType.methodType(int.class, int[].class, int.class, int.class)),
                "GET_AND_BITWISE type");
        // unsupported modes still report their hypothetical method type
        VarHandle svh = MethodHandles.arrayElementVarHandle(String[].class);
        check(svh.accessModeType(AccessMode.GET_AND_BITWISE_OR).equals(
                MethodType.methodType(String.class, String[].class, int.class, String.class)),
                "unsupported bitwise type");
        check(svh.accessModeType(AccessMode.GET_AND_ADD).equals(
                MethodType.methodType(String.class, String[].class, int.class, String.class)),
                "unsupported add type");
        checkThrowsMsg(NullPointerException.class, "accessMode",
                () -> { MethodType t = vh.accessModeType(null); }, "accessModeType null mode");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testEnumContract();
        testMethodNameMatrix();
        testSupportMatrix();
        testAccessModeTypeMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
