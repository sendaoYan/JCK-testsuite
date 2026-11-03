/*
 * @test
 * @summary java.lang.invoke.MethodHandles conformance: white-box + black-box behavior.
 * @run main MethodHandlesTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class MethodHandlesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int doubleIt(int x) { return x * 2; }
    static int add3(int a, int b, int c) { return a + b + c; }
    static boolean isPositive(int x) { return x > 0; }
    static String paren(String s) { return "(" + s + ")"; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MethodHandles");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        String[] names = {"lookup", "publicLookup", "privateLookupIn", "identity", "constant",
                "zero", "empty", "arrayElementGetter", "arrayElementSetter", "arrayLength",
                "arrayConstructor", "insertArguments", "dropArguments", "permuteArguments",
                "filterArguments", "filterReturnValue", "foldArguments", "guardWithTest",
                "catchException", "throwException", "countedLoop", "iteratedLoop", "loop",
                "whileLoop", "doWhileLoop", "tryFinally", "explicitCastArguments",
                "spreadInvoker", "exactInvoker", "invoker",
                "arrayElementVarHandle", "byteArrayViewVarHandle", "byteBufferViewVarHandle"};
        for (String n : names) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // identity / constant / zero
        MethodHandle id = MethodHandles.identity(int.class);
        check((int) id.invokeExact(42) == 42, "identity round trip");
        MethodHandle s = MethodHandles.constant(String.class, "x");
        check("x".equals((String) s.invoke()), "constant String");
        check((int) MethodHandles.constant(int.class, 7).invoke() == 7, "constant int");
        check((int) MethodHandles.zero(int.class).invoke() == 0, "zero int");
        check(MethodHandles.zero(String.class).invoke() == null, "zero reference null");

        // empty: callable, returns nothing
        MethodHandle noop = MethodHandles.empty(MethodType.methodType(void.class));
        noop.invoke();

        // array combinators
        MethodHandle arrayGet = MethodHandles.arrayElementGetter(int[].class);
        int[] arr = {1, 2, 3};
        check((int) arrayGet.invoke(arr, 2) == 3, "arrayElementGetter");
        MethodHandle arrSet = MethodHandles.arrayElementSetter(int[].class);
        arrSet.invoke(arr, 1, 99);
        check(arr[1] == 99, "arrayElementSetter");
        check((int) MethodHandles.arrayLength(int[].class).invoke(new int[7]) == 7, "arrayLength");
        int[] made = (int[]) MethodHandles.arrayConstructor(int[].class).invoke(5);
        check(made.length == 5, "arrayConstructor");

        // insertArguments / dropArguments
        MethodHandle add3 = lookup.findStatic(MethodHandlesTest.class, "add3",
                MethodType.methodType(int.class, int.class, int.class, int.class));
        MethodHandle add12 = MethodHandles.insertArguments(add3, 0, 10, 20);
        check((int) add12.invokeExact(3) == 33, "insertArguments");
        MethodHandle dropped = MethodHandles.dropArguments(add3, 0, String.class);
        check((int) dropped.invoke("ignored", 1, 2, 3) == 6, "dropArguments");

        // permuteArguments swaps arguments
        MethodHandle sub = lookup.findStatic(MethodHandlesTest.class, "sub",
                MethodType.methodType(int.class, int.class, int.class));
        MethodHandle swapped = MethodHandles.permuteArguments(sub,
                MethodType.methodType(int.class, int.class, int.class), 1, 0);
        check((int) swapped.invokeExact(10, 4) == -6, "permuteArguments swaps");

        // filterArguments applies functions to selected arguments
        MethodHandle dbl = lookup.findStatic(MethodHandlesTest.class, "doubleIt",
                MethodType.methodType(int.class, int.class));
        MethodHandle filtered = MethodHandles.filterArguments(add3, 0, dbl);
        check((int) filtered.invokeExact(1, 2, 3) == 7, "filterArguments doubles first");

        // filterReturnValue post-processes the result
        MethodHandle frv = MethodHandles.filterReturnValue(dbl, dbl);
        check((int) frv.invokeExact(3) == 12, "filterReturnValue");

        // foldArguments: combiner runs on the first arg; its result is prepended to the
        // remaining arguments: folded(2,3) == add3(dbl(2), 2, 3) == 9
        MethodHandle folded = MethodHandles.foldArguments(add3, dbl);
        check(folded.type().parameterCount() == 2, "foldArguments shrinks parameter list");
        check((int) folded.invokeExact(2, 3) == 9, "foldArguments");

        // guardWithTest picks a branch
        MethodHandle pos = lookup.findStatic(MethodHandlesTest.class, "isPositive",
                MethodType.methodType(boolean.class, int.class));
        MethodHandle branch = MethodHandles.guardWithTest(pos, dbl, dbl.asType(
                MethodType.methodType(int.class, int.class)));
        check((int) branch.invokeExact(21) == 42, "guardWithTest true branch");
        MethodHandle neg = MethodHandles.guardWithTest(pos,
                MethodHandles.dropArguments(MethodHandles.constant(int.class, 0), 0, int.class),
                MethodHandles.dropArguments(MethodHandles.constant(int.class, -1), 0, int.class));
        check((int) neg.invokeExact(5) == 0 && (int) neg.invokeExact(-5) == -1,
              "guardWithTest both branches");

        // catchException recovers from a thrown exception
        MethodHandle thrower = MethodHandles.throwException(int.class,
                IllegalStateException.class);
        MethodHandle guarded = MethodHandles.catchException(thrower,
                IllegalStateException.class,
                MethodHandles.dropArguments(MethodHandles.constant(int.class, -99), 0,
                        IllegalStateException.class));
        check((int) guarded.invokeExact(new IllegalStateException()) == -99,
              "catchException recovers");

        // throwException actually throws
        try {
            int unreachable = (int) thrower.invokeExact(new IllegalStateException("boom"));
            check(unreachable == 0, "unreachable");
            check(false, "throwException must throw");
        } catch (IllegalStateException e) {
            check("boom".equals(e.getMessage()), "throwException throws ISE");
        check(true, "throwException reached catch");
        }

        // explicitCastArguments widens int -> Object
        MethodHandle boxed = MethodHandles.explicitCastArguments(dbl,
                MethodType.methodType(Object.class, Object.class));
        check(boxed.invoke(21).equals(42), "explicitCastArguments");

        // tryFinally preserves the result through the cleanup:
        // cleanup(Throwable, int result, int arg) -> int
        MethodHandle tf = MethodHandles.tryFinally(dbl,
                lookup.findStatic(MethodHandlesTest.class, "cleanup",
                        MethodType.methodType(int.class, Throwable.class, int.class,
                                int.class)));
        check((int) tf.invokeExact(4) == 8, "tryFinally keeps the result");

        // reflectAs works on a lookup-created handle
        java.lang.reflect.Method m = MethodHandles.reflectAs(java.lang.reflect.Method.class, dbl);
        check(m.getName().equals("doubleIt"), "reflectAs");
    }

    static int sub(int a, int b) { return a - b; }
    static int cleanup(Throwable t, int result, int arg) { return result; }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
