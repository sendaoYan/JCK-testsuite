/*
 * @test
 * @summary java.lang.invoke.VarHandle conformance: white-box + black-box behavior.
 * @run main VarHandleTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
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

    public static class Holder {
        public int x = 5;
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

    static int staticField = 1234;

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
