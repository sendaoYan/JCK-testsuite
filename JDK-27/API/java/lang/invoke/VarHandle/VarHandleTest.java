/*
 * @test
 * @summary java.lang.invoke.VarHandle conformance: white-box + black-box behavior.
 *          Deepened: full access-mode matrix on array/field handles (CAS/CAE/
 *          getAndSet/getAndAdd/getAndBitwise/weak families), long[]/String[]/
 *          boolean[] support matrix, exact AIOOBE/WMTE/CCE/NPE/UOE contracts,
 *          isAccessModeSupported truth table, accessModeType precision,
 *          invoke-exact behavior toggles, static fences, toMethodHandle
 *          round-trips, describeConstable equality.
 * @run main VarHandleTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.AccessMode;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class VarHandleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

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

    public static class Holder {
        public int x = 5;
    }

    /** Deepening target: fields of every var-type family for the support matrix. */
    public static class Owner {
        public int intField = 5;
        public String strField = "s";
        public boolean boolField = true;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.VarHandle");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        String[] names = {"get", "set", "getVolatile", "setVolatile", "getOpaque",
                "setOpaque", "getAcquire", "setRelease", "compareAndSet", "compareAndExchange",
                "weakCompareAndSet", "getAndSet", "getAndAdd", "getAndBitwiseOr",
                "getAndBitwiseAnd", "getAndBitwiseXor", "toMethodHandle", "accessModeType",
                "coordinateTypes", "varType", "describeConstable", "isAccessModeSupported",
                "hasInvokeExactBehavior", "toString", "getAndSetAcquire", "getAndSetRelease"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
        // accessors are final native methods
        Method get = c.getDeclaredMethod("get", Object[].class);
        check(Modifier.isFinal(get.getModifiers()) && Modifier.isNative(get.getModifiers())
              && get.isVarArgs(), "get is final native varargs");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        // array element VarHandle
        VarHandle avh = MethodHandles.arrayElementVarHandle(int[].class);
        int[] arr = {1, 2, 3};
        check(avh.varType() == int.class, "varType");
        check(avh.coordinateTypes().equals(java.util.List.of(int[].class, int.class)),
              "coordinateTypes");
        check(avh.accessModeType(AccessMode.GET).toMethodDescriptorString().equals("([II)I"),
              "accessModeType GET");
        check((int) avh.get(arr, 1) == 2, "get");
        avh.set(arr, 1, 20);
        check(arr[1] == 20, "set");
        avh.setVolatile(arr, 1, 21);
        check((int) avh.getVolatile(arr, 1) == 21, "get/setVolatile");
        avh.setRelease(arr, 1, 22);
        check((int) avh.getAcquire(arr, 1) == 22, "getAcquire/setRelease");
        avh.setOpaque(arr, 1, 23);
        check((int) avh.getOpaque(arr, 1) == 23, "getOpaque/setOpaque");

        // atomic operations
        check(avh.compareAndSet(arr, 1, 23, 99), "compareAndSet success");
        check(!avh.compareAndSet(arr, 1, 23, 0), "compareAndSet failure");
        check((int) avh.get(arr, 1) == 99, "value after CAS");
        check((int) avh.compareAndExchange(arr, 1, 99, 55) == 99
              && (int) avh.get(arr, 1) == 55, "compareAndExchange");
        check((int) avh.getAndSet(arr, 1, 7) == 55, "getAndSet returns old");
        check((int) avh.getAndAdd(arr, 1, 3) == 7 && (int) avh.get(arr, 1) == 10,
              "getAndAdd");
        int oldOr = (int) avh.getAndBitwiseOr(arr, 1, 2);
        check(oldOr == 10 && (int) avh.get(arr, 1) == (10 | 2), "getAndBitwiseOr");
        check(avh.weakCompareAndSet(arr, 1, 10 | 2, 0), "weakCompareAndSet");
        check((int) avh.get(arr, 1) == 0, "value after weak CAS");

        // toMethodHandle exposes an equivalent MethodHandle
        MethodHandle getter = avh.toMethodHandle(AccessMode.GET);
        check(getter.type().toMethodDescriptorString().equals("([II)I"),
              "toMethodHandle type");
        check((int) getter.invokeExact(arr, 0) == 1, "toMethodHandle invocation");
        MethodHandle setter = avh.toMethodHandle(AccessMode.SET);
        setter.invokeExact(arr, 0, 100);
        check(arr[0] == 100, "toMethodHandle SET writes");

        // describeConstable yields a resolvable VarHandleDesc
        java.util.Optional<VarHandle.VarHandleDesc> nd = avh.describeConstable();
        check(nd.isPresent(), "describeConstable present");
        VarHandle resolved = nd.get().resolveConstantDesc(MethodHandles.lookup());
        int[] other = {9, 8};
        check((int) resolved.get(other, 1) == 8, "resolved VarHandle usable");

        // instance field VarHandle from a Lookup
        VarHandle fvh = MethodHandles.lookup().findVarHandle(Holder.class, "x", int.class);
        Holder h = new Holder();
        check((int) fvh.get(h) == 5, "field get");
        fvh.set(h, 6);
        check(h.x == 6, "field set");
        check(fvh.coordinateTypes().equals(java.util.List.of(Holder.class)),
              "field coordinateTypes");
        check((int) fvh.getAndAdd(h, 1) == 6 && h.x == 7, "field getAndAdd");
        check(fvh.compareAndSet(h, 7, 70), "field CAS");

        // static field VarHandle has no receiver coordinate
        VarHandle svh = MethodHandles.lookup().findStaticVarHandle(VarHandleTest.class,
                "staticField", int.class);
        check(svh.coordinateTypes().isEmpty(), "static field has no coordinates");
        check((int) svh.get() == 1234, "static get");
        svh.set(4321);
        check(staticField == 4321, "static set");

        // toString describes type and coordinates
        check(avh.toString().contains("int"), "toString mentions type");

        // failed access mode for wrong coordinates throws IAE
        try {
            avh.get(new Object(), 0);
            check(false, "wrong coordinates rejected");
        } catch (ClassCastException | IllegalArgumentException e) {
            check(true, "wrong coordinates throw");
        }
    }

    // ---- Part 3: deepened contract coverage ----

    static void testArrayAtomicMatrix() throws Throwable {
        VarHandle avh = MethodHandles.arrayElementVarHandle(int[].class);
        int[] a = {100};
        // compareAndExchange: success returns the expected (witness) value and updates;
        // failure returns the current value and leaves the element untouched
        check((int) avh.compareAndExchange(a, 0, 100, 200) == 100, "CAE success returns expected");
        check(a[0] == 200, "CAE success updates");
        check((int) avh.compareAndExchange(a, 0, 100, 300) == 200, "CAE failure returns current");
        check(a[0] == 200, "CAE failure leaves value");
        check((int) avh.compareAndExchangeAcquire(a, 0, 200, 201) == 200, "CAE acquire success");
        check((int) avh.compareAndExchangeAcquire(a, 0, 999, 1) == 201, "CAE acquire failure");
        check(a[0] == 201, "CAE acquire failure leaves value");
        check((int) avh.compareAndExchangeRelease(a, 0, 201, 202) == 201, "CAE release success");
        check((int) avh.compareAndExchangeRelease(a, 0, 999, 1) == 202, "CAE release failure");
        check(a[0] == 202, "CAE release failure leaves value");
        // getAndSet acquire/release return the previous value
        check((int) avh.getAndSetAcquire(a, 0, 300) == 202, "getAndSetAcquire returns old");
        check((int) avh.getAndSetRelease(a, 0, 301) == 300, "getAndSetRelease returns old");
        check(a[0] == 301, "value after getAndSet family");
        // getAndAdd plain/acquire/release: previous value returned, exact sum written
        check((int) avh.getAndAdd(a, 0, 10) == 301 && a[0] == 311, "getAndAdd plain");
        check((int) avh.getAndAddAcquire(a, 0, 10) == 311 && a[0] == 321, "getAndAddAcquire");
        check((int) avh.getAndAddRelease(a, 0, 10) == 321 && a[0] == 331, "getAndAddRelease");
        check((int) avh.getAndAdd(a, 0, -31) == 331 && a[0] == 300, "getAndAdd negative delta");
        // VarHandle has no getAndIncrement/getAndDecrement (javap-verified, unlike
        // AtomicXxxFieldUpdater); the idiom is getAndAdd(+1)/getAndAdd(-1)
        check((int) avh.getAndAdd(a, 0, 1) == 300 && a[0] == 301, "increment via getAndAdd(+1)");
        check((int) avh.getAndAdd(a, 0, -1) == 301 && a[0] == 300, "decrement via getAndAdd(-1)");
        // bitwise family: plain/acquire/release variants all return old and write exact result
        int expect;
        check((int) avh.getAndBitwiseOr(a, 0, 0x0F) == 300 && a[0] == (300 | 0x0F), "getAndBitwiseOr");
        expect = 300 | 0x0F;
        check((int) avh.getAndBitwiseOrAcquire(a, 0, 0xF0) == expect && a[0] == (expect | 0xF0),
                "getAndBitwiseOrAcquire");
        expect = expect | 0xF0;
        check((int) avh.getAndBitwiseOrRelease(a, 0, 0x100) == expect && a[0] == (expect | 0x100),
                "getAndBitwiseOrRelease");
        expect = expect | 0x100;
        check((int) avh.getAndBitwiseAnd(a, 0, 0xFF) == expect && a[0] == (expect & 0xFF),
                "getAndBitwiseAnd");
        expect = expect & 0xFF;
        check((int) avh.getAndBitwiseAndAcquire(a, 0, 0x0F) == expect && a[0] == (expect & 0x0F),
                "getAndBitwiseAndAcquire");
        expect = expect & 0x0F;
        check((int) avh.getAndBitwiseAndRelease(a, 0, 0x07) == expect && a[0] == (expect & 0x07),
                "getAndBitwiseAndRelease");
        expect = expect & 0x07;
        check((int) avh.getAndBitwiseXor(a, 0, 0x03) == expect && a[0] == (expect ^ 0x03),
                "getAndBitwiseXor");
        expect = expect ^ 0x03;
        check((int) avh.getAndBitwiseXorAcquire(a, 0, 0x01) == expect && a[0] == (expect ^ 0x01),
                "getAndBitwiseXorAcquire");
        expect = expect ^ 0x01;
        check((int) avh.getAndBitwiseXorRelease(a, 0, 0x02) == expect && a[0] == (expect ^ 0x02),
                "getAndBitwiseXorRelease");
        // weak CAS variants: a matching pair same-thread deterministically updates; weak
        // semantics permit spurious failure, so only the resulting value is asserted
        expect = expect ^ 0x02;
        avh.weakCompareAndSetPlain(a, 0, expect, 7);
        check(a[0] == 7, "weakCompareAndSetPlain updates value");
        avh.weakCompareAndSetAcquire(a, 0, 7, 8);
        check(a[0] == 8, "weakCompareAndSetAcquire updates value");
        avh.weakCompareAndSetRelease(a, 0, 8, 9);
        check(a[0] == 9, "weakCompareAndSetRelease updates value");
        avh.weakCompareAndSet(a, 0, 8, 10);
        check(a[0] == 9, "weakCompareAndSet mismatch leaves value");
        avh.weakCompareAndSetPlain(a, 0, 8, 10);
        check(a[0] == 9, "weakCompareAndSetPlain mismatch leaves value");
    }

    static void testOtherArrayTypes() throws Throwable {
        // long[]: numeric and bitwise access modes
        VarHandle lvh = MethodHandles.arrayElementVarHandle(long[].class);
        check(lvh.varType() == long.class, "long[] varType");
        long[] la = {10L, 15L};
        lvh.set(la, 1, 20L);
        check((long) lvh.get(la, 1) == 20L, "long[] get/set");
        check((long) lvh.getAndAdd(la, 1, 5L) == 20L && la[1] == 25L, "long[] getAndAdd");
        check((long) lvh.getAndBitwiseAnd(la, 1, 12L) == 25L && la[1] == (25L & 12L),
                "long[] getAndBitwiseAnd");
        check((long) lvh.getAndBitwiseOr(la, 1, 2L) == 8L && la[1] == (8L | 2L),
                "long[] getAndBitwiseOr");
        check((long) lvh.getAndBitwiseXor(la, 1, 10L) == 10L && la[1] == (10L ^ 10L),
                "long[] getAndBitwiseXor");

        // String[]: reference elements support get/set/CAS/CAE/getAndSet but not numeric/bitwise
        VarHandle svh = MethodHandles.arrayElementVarHandle(String[].class);
        String[] sa = {"a", "b"};
        svh.setVolatile(sa, 1, "B");
        check("B".equals((String) svh.getVolatile(sa, 1)), "String[] volatile round-trip");
        check(svh.compareAndSet(sa, 1, "B", "C"), "String[] CAS success");
        check(sa[1].equals("C"), "String[] CAS value");
        check(!svh.compareAndSet(sa, 1, "B", "D") && sa[1].equals("C"), "String[] CAS failure");
        check("C".equals((String) svh.compareAndExchange(sa, 1, "C", "E")) && sa[1].equals("E"),
                "String[] CAE");
        check("a".equals((String) svh.getAndSet(sa, 0, "z")) && sa[0].equals("z"),
                "String[] getAndSet");
        checkThrowsMsg(UnsupportedOperationException.class, "getAndBitwiseOr",
                () -> svh.getAndBitwiseOr(sa, 0, "x"), "String[] bitwise unsupported");

        // boolean[]: bitwise modes work, numeric add does not
        VarHandle bvh = MethodHandles.arrayElementVarHandle(boolean[].class);
        check(bvh.varType() == boolean.class, "boolean[] varType");
        check(bvh.coordinateTypes().equals(java.util.List.of(boolean[].class, int.class)),
                "boolean[] coordinates");
        boolean[] ba = {true, false};
        bvh.set(ba, 1, true);
        check((boolean) bvh.get(ba, 1), "boolean[] get/set");
        check((boolean) bvh.getAndBitwiseOr(ba, 1, false) && ba[1], "boolean[] getAndBitwiseOr");
        boolean o = (boolean) bvh.getAndBitwiseAnd(ba, 0, false);
        check(o && !ba[0], "boolean[] getAndBitwiseAnd old+new");
        o = (boolean) bvh.getAndBitwiseXor(ba, 0, true);
        check(!o && ba[0], "boolean[] getAndBitwiseXor old+new");
        checkThrowsMsg(UnsupportedOperationException.class, "getAndAdd",
                () -> bvh.getAndAdd(ba, 0, 1), "boolean[] getAndAdd unsupported");
        // toString describes varType and the array coordinate type
        check(svh.toString().contains("varType=java.lang.String"), "String[] toString varType");
        check(svh.toString().contains("java.lang.String;"), "String[] toString array class");
        check(lvh.toString().contains("varType=long"), "long[] toString varType");
        check(bvh.toString().contains("varType=boolean"), "boolean[] toString varType");
    }

    static void testArrayBoundsAndTypeErrors() throws Throwable {
        VarHandle avh = MethodHandles.arrayElementVarHandle(int[].class);
        int[] arr = {1, 2, 3};
        // out-of-bounds index reports the exact index and array length
        checkThrowsMsg(ArrayIndexOutOfBoundsException.class, "Index 5 out of bounds for length 3",
                () -> { int v = (int) avh.get(arr, 5); }, "get bounds +5");
        checkThrowsMsg(ArrayIndexOutOfBoundsException.class, "Index -1 out of bounds for length 3",
                () -> { int v = (int) avh.get(arr, -1); }, "get bounds -1");
        checkThrowsMsg(ArrayIndexOutOfBoundsException.class, "Index 7 out of bounds for length 3",
                () -> avh.set(arr, 7, 9), "set bounds");
        checkThrowsMsg(ArrayIndexOutOfBoundsException.class, "Index 5 out of bounds for length 3",
                () -> { int v = (int) avh.getVolatile(arr, 5); }, "getVolatile bounds");
        checkThrowsMsg(ArrayIndexOutOfBoundsException.class, "Index 5 out of bounds for length 3",
                () -> { boolean b = avh.compareAndSet(arr, 5, 1, 2); }, "compareAndSet bounds");
        checkThrowsMsg(ArrayIndexOutOfBoundsException.class, "Index 5 out of bounds for length 3",
                () -> { int v = (int) avh.getAndSet(arr, 5, 1); }, "getAndSet bounds");
        // call-site shape mismatches are WrongMethodTypeException (invoke behavior)
        checkThrowsMsg(WrongMethodTypeException.class, "cannot convert",
                () -> { String s = (String) avh.get(arr, 0); }, "get cast to String");
        checkThrowsMsg(WrongMethodTypeException.class, "cannot convert",
                () -> { Object r = avh.get(arr); }, "get missing coordinate");
        checkThrowsMsg(WrongMethodTypeException.class, "cannot convert",
                () -> { Object r = avh.get(arr, 0, 9); }, "get extra coordinate");
        checkThrowsMsg(WrongMethodTypeException.class, "cannot convert",
                () -> avh.set(arr, 0, "x"), "set wrong value type");
        checkThrowsMsg(WrongMethodTypeException.class, "cannot convert",
                () -> avh.set(arr, 0, null), "set null int value");
        // wrong coordinate type: adapted by ClassCastException (invoke behavior)
        checkThrowsMsg(ClassCastException.class, "Cannot cast java.lang.String to [I",
                () -> { Object r = avh.get("x", 0); }, "wrong array coordinate");
        // null array
        checkThrowsMsg(NullPointerException.class, "is null",
                () -> { Object r = avh.get(null, 0); }, "null array");
    }

    static void testFieldHandleContract() throws Throwable {
        VarHandle vh = MethodHandles.lookup().findVarHandle(Owner.class, "intField", int.class);
        Owner o = new Owner();
        check((int) vh.get(o) == 5, "field plain get initial");
        vh.setVolatile(o, 6);
        check((int) vh.getVolatile(o) == 6, "field volatile round-trip");
        vh.setRelease(o, 7);
        check((int) vh.getAcquire(o) == 7, "field release/acquire round-trip");
        vh.setOpaque(o, 8);
        check((int) vh.getOpaque(o) == 8, "field opaque round-trip");
        check(o.intField == 8, "field writes visible to plain read");
        // numeric update family
        check((int) vh.getAndAdd(o, 1) == 8 && o.intField == 9, "field getAndAdd");
        check((int) vh.getAndAddAcquire(o, 1) == 9 && o.intField == 10, "field getAndAddAcquire");
        check((int) vh.getAndAddRelease(o, 1) == 10 && o.intField == 11, "field getAndAddRelease");
        check((int) vh.getAndAdd(o, -2) == 11 && o.intField == 9, "field getAndAdd negative");
        // bitwise family on the int field
        check((int) vh.getAndBitwiseOr(o, 0x10) == 9 && o.intField == (9 | 0x10),
                "field getAndBitwiseOr");
        check((int) vh.getAndBitwiseAnd(o, 0x18) == (9 | 0x10)
                && o.intField == ((9 | 0x10) & 0x18), "field getAndBitwiseAnd");
        check((int) vh.getAndBitwiseXor(o, 0x01) == ((9 | 0x10) & 0x18)
                && o.intField == (((9 | 0x10) & 0x18) ^ 1), "field getAndBitwiseXor");
        // CAS / CAE / weak CAS / getAndSet families
        check(vh.compareAndSet(o, 25, 30) && o.intField == 30, "field CAS success");
        check(!vh.compareAndSet(o, 25, 31) && o.intField == 30, "field CAS failure");
        check((int) vh.compareAndExchange(o, 30, 40) == 30 && o.intField == 40,
                "field CAE success");
        check((int) vh.compareAndExchangeAcquire(o, 40, 41) == 40 && o.intField == 41,
                "field CAE acquire");
        check((int) vh.compareAndExchangeRelease(o, 41, 42) == 41 && o.intField == 42,
                "field CAE release");
        check((int) vh.compareAndExchange(o, 41, 43) == 42 && o.intField == 42,
                "field CAE failure returns current");
        vh.weakCompareAndSetPlain(o, 42, 50);
        check(o.intField == 50, "field weakCompareAndSetPlain updates");
        vh.weakCompareAndSetAcquire(o, 50, 51);
        check(o.intField == 51, "field weakCompareAndSetAcquire updates");
        vh.weakCompareAndSetRelease(o, 51, 52);
        check(o.intField == 52, "field weakCompareAndSetRelease updates");
        check((int) vh.getAndSet(o, 60) == 52, "field getAndSet returns old");
        check((int) vh.getAndSetAcquire(o, 61) == 60, "field getAndSetAcquire returns old");
        check((int) vh.getAndSetRelease(o, 62) == 61, "field getAndSetRelease returns old");
        check(o.intField == 62, "field getAndSet family final value");
        // accessModeType precision (field form: receiver + value coordinates)
        check(vh.accessModeType(AccessMode.GET).equals(MethodType.methodType(int.class, Owner.class)),
                "field GET type");
        check(vh.accessModeType(AccessMode.SET)
                .equals(MethodType.methodType(void.class, Owner.class, int.class)),
                "field SET type");
        check(vh.accessModeType(AccessMode.COMPARE_AND_SET)
                .equals(MethodType.methodType(boolean.class, Owner.class, int.class, int.class)),
                "field CAS type");
        check(vh.accessModeType(AccessMode.COMPARE_AND_EXCHANGE)
                .equals(MethodType.methodType(int.class, Owner.class, int.class, int.class)),
                "field CAE type");
        check(vh.accessModeType(AccessMode.GET_AND_ADD)
                .equals(MethodType.methodType(int.class, Owner.class, int.class)),
                "field GET_AND_ADD type");
        check(vh.accessModeType(AccessMode.GET_AND_BITWISE_OR)
                .equals(MethodType.methodType(int.class, Owner.class, int.class)),
                "field BITWISE_OR type");
        // wrong receiver is a ClassCastException naming both classes
        checkThrowsMsg(ClassCastException.class, "Cannot cast java.lang.String to VarHandleTest$Owner",
                () -> { Object r = vh.get("str"); }, "field wrong receiver");
        checkThrowsMsg(ClassCastException.class, "Cannot cast java.lang.Object to VarHandleTest$Owner",
                () -> { Object r = vh.get(new Object()); }, "field Object receiver");
        checkThrows(NullPointerException.class, () -> { Object r = vh.get((Object) null); },
                "field null receiver");
        checkThrowsMsg(WrongMethodTypeException.class, "cannot convert",
                () -> vh.set(o, "x"), "field set wrong value type");
        checkThrowsMsg(WrongMethodTypeException.class, "cannot convert",
                () -> vh.set(o, null), "field set null value");
        checkThrowsMsg(WrongMethodTypeException.class, "cannot convert",
                () -> { int i = (int) vh.get(o, 1); }, "field extra coordinate");
        // toString describes varType + coordinate class but (measured) omits the field name
        check(vh.toString().contains("varType=int"), "field toString varType");
        check(vh.toString().contains("Owner"), "field toString coordinate class");
        check(!vh.toString().contains("intField"), "field toString omits field name");
    }

    static void testAccessModeSupportMatrix() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        VarHandle intArr = MethodHandles.arrayElementVarHandle(int[].class);
        VarHandle strArr = MethodHandles.arrayElementVarHandle(String[].class);
        VarHandle boolArr = MethodHandles.arrayElementVarHandle(boolean[].class);
        VarHandle longArr = MethodHandles.arrayElementVarHandle(long[].class);
        VarHandle intField = lookup.findVarHandle(Owner.class, "intField", int.class);
        VarHandle strField = lookup.findVarHandle(Owner.class, "strField", String.class);
        VarHandle boolField = lookup.findVarHandle(Owner.class, "boolField", boolean.class);
        int nIntArr = 0, nStrArr = 0, nBoolArr = 0, nLongArr = 0;
        int nIntF = 0, nStrF = 0, nBoolF = 0;
        for (AccessMode m : AccessMode.values()) {
            if (intArr.isAccessModeSupported(m)) nIntArr++;
            if (strArr.isAccessModeSupported(m)) nStrArr++;
            if (boolArr.isAccessModeSupported(m)) nBoolArr++;
            if (longArr.isAccessModeSupported(m)) nLongArr++;
            if (intField.isAccessModeSupported(m)) nIntF++;
            if (strField.isAccessModeSupported(m)) nStrF++;
            if (boolField.isAccessModeSupported(m)) nBoolF++;
        }
        check(nIntArr == 31 && nLongArr == 31, "int[]/long[] support all 31 modes");
        check(nIntF == 31, "int field supports all 31 modes");
        check(nStrArr == 19 && nStrF == 19, "String[]/String field support 19 (no add, no bitwise)");
        check(nBoolArr == 28 && nBoolF == 28, "boolean[]/boolean field support 28 (no add)");
        // precise matrix cells
        check(intField.isAccessModeSupported(AccessMode.GET_AND_ADD), "int field GET_AND_ADD true");
        check(intField.isAccessModeSupported(AccessMode.GET_AND_BITWISE_OR), "int field BITWISE_OR true");
        check(intField.isAccessModeSupported(AccessMode.GET_AND_BITWISE_XOR_RELEASE),
                "int field XOR_RELEASE true");
        check(!strField.isAccessModeSupported(AccessMode.GET_AND_ADD_ACQUIRE),
                "String field add acquire false");
        check(!strField.isAccessModeSupported(AccessMode.GET_AND_BITWISE_OR),
                "String field BITWISE_OR false");
        check(!strField.isAccessModeSupported(AccessMode.GET_AND_BITWISE_XOR_ACQUIRE),
                "String field XOR_ACQUIRE false");
        check(!strArr.isAccessModeSupported(AccessMode.GET_AND_ADD_RELEASE),
                "String[] add release false");
        check(!boolField.isAccessModeSupported(AccessMode.GET_AND_ADD), "boolean field add false");
        check(boolField.isAccessModeSupported(AccessMode.GET_AND_BITWISE_AND),
                "boolean field BITWISE_AND true");
        check(boolArr.isAccessModeSupported(AccessMode.GET_AND_BITWISE_XOR),
                "boolean[] BITWISE_XOR true");
        // reference var types keep the full get/set/CAS/CAE/weak family
        check(strField.isAccessModeSupported(AccessMode.COMPARE_AND_EXCHANGE_ACQUIRE),
                "String field CAE acquire true");
        check(strField.isAccessModeSupported(AccessMode.WEAK_COMPARE_AND_SET_RELEASE),
                "String field weak CAS release true");
        // unsupported modes still expose their (hypothetical) access mode type
        check(strArr.accessModeType(AccessMode.GET_AND_BITWISE_OR)
                .equals(MethodType.methodType(String.class, String[].class, int.class, String.class)),
                "unsupported mode still has a type");
        checkThrowsMsg(NullPointerException.class, "accessMode",
                () -> { boolean b = intArr.isAccessModeSupported(null); },
                "isAccessModeSupported null");
        checkThrowsMsg(NullPointerException.class, "accessMode",
                () -> { MethodType t = intArr.accessModeType(null); }, "accessModeType null");
    }

    static void testInvokeExactBehavior() throws Throwable {
        VarHandle avh = MethodHandles.arrayElementVarHandle(int[].class);
        int[] a = {7};
        check(!avh.hasInvokeExactBehavior(), "default VarHandle has invoke behavior");
        VarHandle exact = avh.withInvokeExactBehavior();
        check(exact.hasInvokeExactBehavior(), "withInvokeExactBehavior flags exact");
        check(exact != avh, "withInvokeExactBehavior returns a new handle");
        check((int) exact.get(a, 0) == 7, "exact handle plain get");
        exact.set(a, 0, 8);
        check((int) avh.get(a, 0) == 8, "exact write visible through default handle");
        // invoke behavior adapts boxed coordinate/value, exact behavior does not
        check((int) avh.get(a, Integer.valueOf(0)) == 8, "invoke behavior accepts boxed coordinate");
        checkThrowsMsg(WrongMethodTypeException.class, "handle's method type",
                () -> { int i = (int) exact.get(a, Integer.valueOf(0)); },
                "exact behavior rejects boxing");
        checkThrowsMsg(WrongMethodTypeException.class, "handle's method type",
                () -> exact.set(a, 0, null), "exact behavior rejects null value");
        VarHandle back = exact.withInvokeBehavior();
        check(!back.hasInvokeExactBehavior(), "withInvokeBehavior restores invoke behavior");
        check((int) back.get(a, Integer.valueOf(0)) == 8, "restored handle adapts boxing");
    }

    static void testStaticFences() {
        VarHandle.fullFence();
        VarHandle.acquireFence();
        VarHandle.releaseFence();
        VarHandle.loadLoadFence();
        VarHandle.storeStoreFence();
        check(true, "static ordering fences execute without failure");
    }

    static void testToMethodHandleMatrix() throws Throwable {
        VarHandle avh = MethodHandles.arrayElementVarHandle(int[].class);
        int[] a = {10, 20};
        MethodHandle get = avh.toMethodHandle(AccessMode.GET);
        check(get.type().equals(MethodType.methodType(int.class, int[].class, int.class)),
                "toMethodHandle GET type");
        check((int) get.invokeExact(a, 1) == 20, "toMethodHandle GET invoke");
        MethodHandle set = avh.toMethodHandle(AccessMode.SET);
        set.invokeExact(a, 1, 21);
        check(a[1] == 21, "toMethodHandle SET invoke");
        MethodHandle cas = avh.toMethodHandle(AccessMode.COMPARE_AND_SET);
        check((boolean) cas.invokeExact(a, 1, 21, 22), "toMethodHandle CAS success");
        check(!(boolean) cas.invokeExact(a, 1, 21, 23), "toMethodHandle CAS failure");
        check(a[1] == 22, "toMethodHandle CAS value");
        MethodHandle cae = avh.toMethodHandle(AccessMode.COMPARE_AND_EXCHANGE);
        check((int) cae.invokeExact(a, 1, 22, 30) == 22 && a[1] == 30, "toMethodHandle CAE");
        MethodHandle gas = avh.toMethodHandle(AccessMode.GET_AND_SET);
        check((int) gas.invokeExact(a, 1, 40) == 30 && a[1] == 40, "toMethodHandle GET_AND_SET");
        MethodHandle gaa = avh.toMethodHandle(AccessMode.GET_AND_ADD);
        check((int) gaa.invokeExact(a, 1, 5) == 40 && a[1] == 45, "toMethodHandle GET_AND_ADD");
        MethodHandle gbo = avh.toMethodHandle(AccessMode.GET_AND_BITWISE_OR);
        check((int) gbo.invokeExact(a, 1, 0x10) == 45 && a[1] == (45 | 0x10),
                "toMethodHandle BITWISE_OR");
        // unsupported mode: a handle is still produced; invoking it throws UOE
        VarHandle svh = MethodHandles.arrayElementVarHandle(String[].class);
        MethodHandle bad = svh.toMethodHandle(AccessMode.GET_AND_BITWISE_OR);
        String[] sa = {"a"};
        checkThrowsMsg(UnsupportedOperationException.class, "getAndBitwiseOr",
                () -> { Object r = bad.invoke(sa, 0, "x"); }, "unsupported mode handle invocation");
        checkThrowsMsg(NullPointerException.class, "accessMode",
                () -> { MethodHandle m = avh.toMethodHandle(null); }, "toMethodHandle null mode");
    }

    static void testDescribeConstableDeep() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        VarHandle avh = MethodHandles.arrayElementVarHandle(int[].class);
        VarHandle.VarHandleDesc ad = avh.describeConstable().orElseThrow();
        check(ad.equals(VarHandle.VarHandleDesc.ofArray(ConstantDescs.CD_int.arrayType())),
                "array describeConstable equals ofArray");
        check(ad.toString().equals("VarHandleDesc[int[][]]"), "array desc toString exact");
        VarHandle resolved = ad.resolveConstantDesc(lookup);
        check(resolved != avh, "resolved handle is a distinct instance");
        check(!resolved.equals(avh), "VarHandle defines no equals");
        check(resolved.toString().equals(avh.toString()), "resolved toString matches original");
        check(resolved.accessModeType(AccessMode.GET).equals(avh.accessModeType(AccessMode.GET)),
                "resolved accessModeType matches original");
        int[] a = {5};
        check((int) resolved.get(a, 0) == 5, "resolved array handle works");
        VarHandle fvh = lookup.findVarHandle(Owner.class, "intField", int.class);
        VarHandle.VarHandleDesc fd = fvh.describeConstable().orElseThrow();
        check(fd.equals(VarHandle.VarHandleDesc.ofField(
                ClassDesc.of("VarHandleTest").nested("Owner"), "intField", ConstantDescs.CD_int)),
                "field describeConstable equals ofField");
        check(fd.toString().equals("VarHandleDesc[VarHandleTest$Owner.intField:int]"),
                "field desc toString exact");
    }

    static int staticField = 1234;

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testArrayAtomicMatrix();
        testOtherArrayTypes();
        testArrayBoundsAndTypeErrors();
        testFieldHandleContract();
        testAccessModeSupportMatrix();
        testInvokeExactBehavior();
        testStaticFences();
        testToMethodHandleMatrix();
        testDescribeConstableDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
