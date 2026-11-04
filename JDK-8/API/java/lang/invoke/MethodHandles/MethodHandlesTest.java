/*
 * @test
 * @summary java.lang.invoke.MethodHandles conformance: full contract coverage.
 *          Lookup (lookup/publicLookup/lookupClass/lookupModes/in/toString), find
 *          (static/virtual/constructor/static+instance getter+setter/special/bind),
 *          unreflect (method/constructor/getter/setter), revealDirect/reflectAs,
 *          identity/constant, arrayElementGetter/Setter + validation matrix,
 *          insertArguments/dropArguments/filterArguments/filterReturnValue/
 *          foldArguments/guardWithTest/catchException/throwException/permuteArguments/
 *          explicitCastArguments, and exactInvoker/invoker/spreadInvoker.
 * @run main MethodHandlesTest
 */

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

public class MethodHandlesTest {
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

    // --- reflection targets ---
    static int add2(int a, int b) { return a + b; }
    static int sub(int a, int b) { return a - b; }
    static int negate(int a) { return -a; }
    static boolean isPositive(int a) { return a > 0; }
    static int mayThrow(int x) throws IOException {
        if (x < 0) throw new IOException("negative " + x);
        return x;
    }
    static int recover(IOException e, int x) { return 999; }

    static class Parent { int val() { return 1; } }
    static class Child extends Parent { int val() { return 2; } }
    static class Box { int x; static int s; }

    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    static MethodHandle stat(String name, MethodType mt) throws Exception {
        return LOOKUP.findStatic(MethodHandlesTest.class, name, mt);
    }

    static void testLookup() throws Exception {
        MethodHandles.Lookup l = MethodHandles.lookup();
        check(l.lookupClass() == MethodHandlesTest.class, "lookup lookupClass");
        int modes = l.lookupModes();
        check((modes & MethodHandles.Lookup.PUBLIC) != 0
                && (modes & MethodHandles.Lookup.PRIVATE) != 0
                && (modes & MethodHandles.Lookup.PROTECTED) != 0
                && (modes & MethodHandles.Lookup.PACKAGE) != 0, "lookup all modes");
        check(l.toString() != null, "lookup toString");

        MethodHandles.Lookup pub = MethodHandles.publicLookup();
        check(pub.lookupClass() == Object.class, "publicLookup lookupClass Object");
        check(pub.lookupModes() == MethodHandles.Lookup.PUBLIC,
                "publicLookup only PUBLIC mode");
        check(pub.toString() != null, "publicLookup toString");

        // in() narrows the lookup class
        MethodHandles.Lookup same = l.in(MethodHandlesTest.class);
        check(same.lookupClass() == MethodHandlesTest.class, "in(same class)");
        check(same.lookupModes() == l.lookupModes(), "in(same class) keeps modes");

        MethodHandles.Lookup narrowed = l.in(String.class);
        check(narrowed.lookupClass() == String.class, "in() changes lookupClass");
        check((narrowed.lookupModes() & MethodHandles.Lookup.PUBLIC) != 0,
                "in(String) keeps PUBLIC");
        check((narrowed.lookupModes() & (MethodHandles.Lookup.PRIVATE
                | MethodHandles.Lookup.PACKAGE)) == 0,
                "in(String) drops private/package modes");
    }

    static void testFind() throws Throwable {
        MethodHandle add = stat("add2", MethodType.methodType(int.class, int.class, int.class));
        check((int) add.invokeExact(2, 3) == 5, "findStatic invoke");

        MethodHandle virt = LOOKUP.findVirtual(Child.class, "val", MethodType.methodType(int.class));
        check((int) virt.invoke(new Child()) == 2, "findVirtual invoke");

        MethodHandle ctor = LOOKUP.findConstructor(Box.class, MethodType.methodType(void.class));
        Box b = (Box) ctor.invoke();
        check(b != null, "findConstructor invoke");

        MethodHandle getter = LOOKUP.findGetter(Box.class, "x", int.class);
        MethodHandle setter = LOOKUP.findSetter(Box.class, "x", int.class);
        setter.invoke(b, 11);
        check((int) getter.invoke(b) == 11, "findGetter/findSetter");

        MethodHandle sget = LOOKUP.findStaticGetter(Box.class, "s", int.class);
        MethodHandle sset = LOOKUP.findStaticSetter(Box.class, "s", int.class);
        sset.invoke(7);
        check((int) sget.invoke() == 7, "findStaticGetter/Setter");

        // findSpecial invokes the superclass version; the lookup must be from Child
        MethodHandle special = LOOKUP.in(Child.class).findSpecial(Parent.class, "val",
                MethodType.methodType(int.class), Child.class);
        check((int) special.invoke(new Child()) == 1, "findSpecial super version");

        // bind() finds a virtual method by name and binds the receiver
        MethodHandle bound = LOOKUP.bind(new Child(), "val", MethodType.methodType(int.class));
        check(bound.type().parameterCount() == 0, "bind binds receiver");
        check((int) bound.invoke() == 2, "bind invoke");

        // discovery errors
        checkThrows(NoSuchMethodException.class,
                () -> LOOKUP.findStatic(MethodHandlesTest.class, "nope",
                        MethodType.methodType(int.class)), "findStatic missing NSME");
        checkThrows(NoSuchFieldException.class,
                () -> LOOKUP.findGetter(Box.class, "nope", int.class),
                "findGetter missing NSFE");
        checkThrows(NoSuchMethodException.class,
                () -> LOOKUP.findConstructor(Box.class, MethodType.methodType(int.class)),
                "findConstructor wrong type NSME");
    }

    static void testUnreflect() throws Throwable {
        Method m = MethodHandlesTest.class.getDeclaredMethod("add2", int.class, int.class);
        MethodHandle um = LOOKUP.unreflect(m);
        check((int) um.invokeExact(4, 6) == 10, "unreflect method");

        Constructor<Box> ctor = Box.class.getDeclaredConstructor();
        MethodHandle uc = LOOKUP.unreflectConstructor(ctor);
        Box b = (Box) uc.invoke();
        check(b != null, "unreflectConstructor");

        Field x = Box.class.getDeclaredField("x");
        MethodHandle ug = LOOKUP.unreflectGetter(x);
        MethodHandle us = LOOKUP.unreflectSetter(x);
        us.invoke(b, 33);
        check((int) ug.invoke(b) == 33, "unreflectGetter/Setter");

        Field s = Box.class.getDeclaredField("s");
        MethodHandle usg = LOOKUP.unreflectGetter(s);
        MethodHandle uss = LOOKUP.unreflectSetter(s);
        uss.invoke(9);
        check((int) usg.invoke() == 9, "unreflect static getter/setter");
    }

    static void testRevealDirectReflectAs() throws Throwable {
        MethodHandle add = stat("add2", MethodType.methodType(int.class, int.class, int.class));
        MethodHandleInfo info = LOOKUP.revealDirect(add);
        check(info.getName().equals("add2"), "revealDirect name");
        check(info.getDeclaringClass() == MethodHandlesTest.class, "revealDirect declaring");
        check(info.getMethodType().equals(MethodType.methodType(int.class, int.class, int.class)),
                "revealDirect methodType");
        check(info.getReferenceKind() != 0, "revealDirect referenceKind");

        // non-direct handle -> IAE
        checkThrows(IllegalArgumentException.class,
                () -> LOOKUP.revealDirect(MethodHandles.identity(int.class)),
                "revealDirect non-direct IAE");
        checkThrows(NullPointerException.class,
                () -> LOOKUP.revealDirect(null), "revealDirect null NPE");

        Method m = MethodHandles.reflectAs(Method.class, add);
        check(m.getName().equals("add2"), "reflectAs Method name");
        check(m.getParameterTypes().length == 2, "reflectAs Method params");

        MethodHandle getter = LOOKUP.findGetter(Box.class, "x", int.class);
        Field f = MethodHandles.reflectAs(Field.class, getter);
        check(f.getName().equals("x"), "reflectAs Field name");
    }

    static void testIdentityConstant() throws Throwable {
        MethodHandle id = MethodHandles.identity(int.class);
        check((int) id.invokeExact(42) == 42, "identity int");
        check(MethodHandles.identity(String.class).invoke("s") == "s", "identity String");

        MethodHandle c = MethodHandles.constant(String.class, "x");
        check("x".equals((String) c.invoke()), "constant String");
        check((int) MethodHandles.constant(int.class, 0).invoke() == 0, "constant int");
        checkThrows(NullPointerException.class,
                () -> MethodHandles.constant(int.class, null), "constant null value NPE");
    }

    static void testArrayElementAccessors() throws Throwable {
        MethodHandle get = MethodHandles.arrayElementGetter(int[].class);
        int[] arr = {1, 2, 3};
        check((int) get.invoke(arr, 2) == 3, "arrayElementGetter int");
        MethodHandle set = MethodHandles.arrayElementSetter(int[].class);
        set.invoke(arr, 1, 99);
        check(arr[1] == 99, "arrayElementSetter int");

        MethodHandle oget = MethodHandles.arrayElementGetter(String[].class);
        String[] sa = {"a", "b"};
        check("b".equals(oget.invoke(sa, 1)), "arrayElementGetter String");

        checkThrows(IllegalArgumentException.class,
                () -> MethodHandles.arrayElementGetter(int.class), "getter non-array IAE");
        checkThrows(IllegalArgumentException.class,
                () -> MethodHandles.arrayElementSetter(String.class), "setter non-array IAE");
        checkThrows(NullPointerException.class,
                () -> MethodHandles.arrayElementGetter(null), "getter null NPE");
        checkThrows(NullPointerException.class,
                () -> MethodHandles.arrayElementSetter(null), "setter null NPE");
    }

    static void testInsertDropArguments() throws Throwable {
        MethodHandle add = stat("add2", MethodType.methodType(int.class, int.class, int.class));
        // insert 10 at position 0 -> (int)int
        MethodHandle ins = MethodHandles.insertArguments(add, 0, 10);
        check((int) ins.invokeExact(5) == 15, "insertArguments");
        // insert at position 1 -> (int)int
        MethodHandle ins2 = MethodHandles.insertArguments(add, 1, 10);
        check((int) ins2.invokeExact(5) == 15, "insertArguments position 1");

        MethodHandle id = MethodHandles.identity(int.class);
        MethodHandle drop = MethodHandles.dropArguments(id, 0, String.class);
        check(drop.type().parameterCount() == 2 && drop.type().parameterType(0) == String.class,
                "dropArguments type");
        check((int) drop.invoke("ignored", 7) == 7, "dropArguments invoke");
        MethodHandle dropList = MethodHandles.dropArguments(id, 0,
                Arrays.asList(String.class, Object.class));
        check(dropList.type().parameterCount() == 3, "dropArguments List");
    }

    static void testFilterArguments() throws Throwable {
        MethodHandle add = stat("add2", MethodType.methodType(int.class, int.class, int.class));
        MethodHandle neg = stat("negate", MethodType.methodType(int.class, int.class));
        // negate the first argument before adding
        MethodHandle filtered = MethodHandles.filterArguments(add, 0, neg);
        check((int) filtered.invoke(5, 3) == -2, "filterArguments negates first arg");
    }

    static void testFilterReturnValue() throws Throwable {
        MethodHandle add = stat("add2", MethodType.methodType(int.class, int.class, int.class));
        MethodHandle neg = stat("negate", MethodType.methodType(int.class, int.class));
        MethodHandle filtered = MethodHandles.filterReturnValue(add, neg);
        check((int) filtered.invoke(2, 3) == -5, "filterReturnValue negates result");
    }

    static void testFoldArguments() throws Throwable {
        MethodHandle add = stat("add2", MethodType.methodType(int.class, int.class, int.class));
        MethodHandle id = MethodHandles.identity(int.class);
        // fold: adapter(a) = add(a, a) -> (int)int
        MethodHandle folded = MethodHandles.foldArguments(add, id);
        check(folded.type().parameterCount() == 1, "foldArguments arity");
        check((int) folded.invoke(5) == 10, "foldArguments add(a,a)");
    }

    static void testGuardWithTest() throws Throwable {
        MethodHandle pos = stat("isPositive", MethodType.methodType(boolean.class, int.class));
        MethodHandle id = MethodHandles.identity(int.class);
        MethodHandle neg = stat("negate", MethodType.methodType(int.class, int.class));
        MethodHandle guard = MethodHandles.guardWithTest(pos, id, neg);
        check((int) guard.invoke(5) == 5, "guardWithTest positive -> target");
        check((int) guard.invoke(-3) == 3, "guardWithTest negative -> fallback");
    }

    static void testCatchException() throws Throwable {
        MethodHandle may = stat("mayThrow", MethodType.methodType(int.class, int.class));
        MethodHandle rec = stat("recover", MethodType.methodType(int.class, IOException.class, int.class));
        MethodHandle guarded = MethodHandles.catchException(may, IOException.class, rec);
        check((int) guarded.invoke(5) == 5, "catchException no throw");
        check((int) guarded.invoke(-1) == 999, "catchException handler invoked");
    }

    static void testThrowException() throws Throwable {
        // throwException builds a handle of type (exType)returnType which throws its argument
        MethodHandle thrower = MethodHandles.throwException(int.class, IOException.class);
        check(thrower.type().equals(MethodType.methodType(int.class, IOException.class)),
                "throwException handle type");
        final IOException ioe = new IOException("via handle");
        try {
            int r = (int) thrower.invokeExact(ioe);
            throw new AssertionError("expected IOException, got " + r);
        } catch (IOException e) {
            passed++;
            check(e == ioe, "throwException throws the supplied exception identity");
        }
    }

    static void testPermuteArguments() throws Throwable {
        MethodHandle sub = stat("sub", MethodType.methodType(int.class, int.class, int.class));
        // swap args: b - a
        MethodHandle swapped = MethodHandles.permuteArguments(sub,
                MethodType.methodType(int.class, int.class, int.class), 1, 0);
        check((int) swapped.invoke(2, 5) == 3, "permuteArguments swap");

        // duplicate first arg (old param 0 comes from new param 0)
        MethodHandle id = MethodHandles.identity(int.class);
        MethodHandle dup = MethodHandles.permuteArguments(id,
                MethodType.methodType(int.class, int.class, int.class), 0);
        check((int) dup.invoke(7, 99) == 7, "permuteArguments duplicate");
    }

    static void testExplicitCastArguments() throws Throwable {
        MethodHandle id = MethodHandles.identity(int.class);
        // cast Integer parameter to int
        MethodHandle cast = MethodHandles.explicitCastArguments(id,
                MethodType.methodType(int.class, Integer.class));
        check((int) cast.invoke(Integer.valueOf(7)) == 7, "explicitCastArguments unbox");
    }

    static void testInvokers() throws Throwable {
        MethodHandle target = MethodHandles.constant(int.class, 42);
        // exactInvoker(type) -> (MethodHandle)int
        MethodHandle exact = MethodHandles.exactInvoker(MethodType.methodType(int.class));
        check((int) exact.invoke(target) == 42, "exactInvoker");

        MethodHandle inv = MethodHandles.invoker(MethodType.methodType(int.class));
        check((int) inv.invoke(target) == 42, "invoker");

        MethodHandle add = stat("add2", MethodType.methodType(int.class, int.class, int.class));
        MethodHandle spread = MethodHandles.spreadInvoker(
                MethodType.methodType(int.class, int.class, int.class), 1);
        check(spread != null, "spreadInvoker returns handle");
    }

    public static void main(String[] args) throws Throwable {
        testLookup();
        testFind();
        testUnreflect();
        testRevealDirectReflectAs();
        testIdentityConstant();
        testArrayElementAccessors();
        testInsertDropArguments();
        testFilterArguments();
        testFilterReturnValue();
        testFoldArguments();
        testGuardWithTest();
        testCatchException();
        testThrowException();
        testPermuteArguments();
        testExplicitCastArguments();
        testInvokers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
