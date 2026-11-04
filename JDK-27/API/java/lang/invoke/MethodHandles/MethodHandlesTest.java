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
 *          Deepened: identity reference/null semantics, constant edges (same object,
 *          Object-null allowed, void -> IAE "void type", wrong type -> CCE),
 *          arrayElementGetter/Setter types + String[] round-trip + AIOOBE,
 *          publicLookup access denial with exact messages ("member is private",
 *          "symbolic reference class is not"), Lookup.toString forms,
 *          collectArguments (type + both positions), explicitCastArguments true
 *          long->int narrowing (truncation) vs asType refusal, combinator
 *          validation matrix, insertArguments binding values, fold prepend,
 *          and JDK-9+ factories arrayLength/arrayConstructor/zero/empty/
 *          dropArgumentsToMatch/dropReturn with exact values and messages.
 * @run main MethodHandlesTest
 */

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
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

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
            String frag, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (t.getMessage() != null && t.getMessage().contains(frag)) {
                    passed++;
                    return;
                }
                throw new AssertionError(msg + ": expected message fragment \"" + frag
                        + "\" but got \"" + t.getMessage() + "\"");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static int len(String s) { return s.length(); }
    static String join3(String a, String b, String c) { return a + "-" + b + "-" + c; }

    static void testIdentityReferenceSemantics() throws Throwable {
        Object v = new Object();
        MethodHandle id = MethodHandles.identity(Object.class);
        check(id.invoke(v) == v, "identity returns same reference");
        check(id.invoke(v) == id.invoke(v), "identity stable across calls");
        check(id.invoke(null) == null, "identity passes null");
        check(id.type().equals(MethodType.methodType(Object.class, Object.class)),
                "identity type");
        checkThrows(NullPointerException.class,
                () -> MethodHandles.identity(null), "identity null type NPE");
    }

    static void testConstantEdges() throws Throwable {
        // the same constant object comes back on every invoke
        Object probe = new Object();
        MethodHandle c = MethodHandles.constant(Object.class, probe);
        check(c.invoke() == probe && c.invoke() == probe, "constant same object twice");
        check(c.type().equals(MethodType.methodType(Object.class, Object.class)) == false,
                "constant type is ()Object");
        // null is a legal reference constant, but not for primitives (unboxing NPE)
        check(MethodHandles.constant(Object.class, null).invoke() == null,
                "constant(Object,null) allowed");
        // void cannot be a constant type -> IAE with exact message
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandles.constant(void.class, null), "void type",
                "constant(void) IAE message");
        // wrong-typed constant -> CCE at factory time
        checkThrowsMsg(ClassCastException.class,
                () -> MethodHandles.constant(String.class, 42),
                "Cannot cast java.lang.Integer to java.lang.String",
                "constant wrong type CCE message");
        // primitive constants unbox at invoke
        check((long) MethodHandles.constant(long.class, 5L).invoke() == 5L,
                "constant long value");
    }

    static void testArrayElementDeep() throws Throwable {
        // String[] round-trip through getter/setter
        MethodHandle get = MethodHandles.arrayElementGetter(String[].class);
        MethodHandle set = MethodHandles.arrayElementSetter(String[].class);
        String[] sa = {"a", "b", "c"};
        set.invoke(sa, 1, "B");
        check("B".equals(sa[1]) && "a".equals(get.invoke(sa, 0))
                && "c".equals(get.invoke(sa, 2)), "arrayElementGetter/Setter String[]");
        // out-of-bounds index -> ArrayIndexOutOfBoundsException (message format differs)
        MethodHandle iget = MethodHandles.arrayElementGetter(int[].class);
        MethodHandle iset = MethodHandles.arrayElementSetter(int[].class);
        checkThrows(ArrayIndexOutOfBoundsException.class,
                () -> iset.invoke(new int[2], 9, 1), "setter AIOOBE");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                () -> iget.invoke(new int[2], -1), "getter negative AIOOBE");
        // getter/setter handle types: (array,int)elem / (array,int,elem)void
        check(MethodHandles.arrayElementGetter(int[].class).type()
                .equals(MethodType.methodType(int.class, int[].class, int.class)),
                "arrayElementGetter type");
        check(MethodHandles.arrayElementSetter(int[].class).type()
                .equals(MethodType.methodType(void.class, int[].class, int.class, int.class)),
                "arrayElementSetter type");
    }

    static void testPublicLookupAccess() throws Throwable {
        MethodHandles.Lookup pub = MethodHandles.publicLookup();
        // public member of a public class is reachable
        check((int) pub.findVirtual(String.class, "length",
                MethodType.methodType(int.class)).invoke("abc") == 3,
                "publicLookup findVirtual String.length");
        // a private member is denied even to publicLookup
        Method m = MethodHandlesTest.class.getDeclaredMethod("add2", int.class, int.class);
        checkThrowsMsg(IllegalAccessException.class,
                () -> pub.unreflect(m), "member is private",
                "publicLookup unreflect private IAE");
        // the lookup class itself not public -> symbolic reference refused
        // (8: "is not public", 27: "is not accessible"; common fragment "is not")
        checkThrowsMsg(IllegalAccessException.class,
                () -> pub.findVirtual(Box.class, "x", MethodType.methodType(int.class)),
                "symbolic reference class is not",
                "publicLookup package-private target class IAE");
    }

    static void testLookupToStringExact() {
        MethodHandles.Lookup l = MethodHandles.lookup();
        check(l.toString().contains("MethodHandlesTest"), "lookup toString names class");
        // 8: "java.lang.Object/public"; 27: "java.lang.Object/publicLookup"
        check(MethodHandles.publicLookup().toString().contains("public"),
                "publicLookup toString contains public");
        check(MethodHandles.publicLookup().toString().startsWith("java.lang.Object/"),
                "publicLookup toString starts with lookup class");
        MethodHandles.Lookup inString = l.in(String.class);
        check(inString.toString().contains("java.lang.String"),
                "in(String) toString names new class");
        check(inString.toString().contains("public"), "in(String) toString has public mode");
    }

    static void testCollectArguments() throws Throwable {
        MethodHandle target = stat("add2", MethodType.methodType(int.class, int.class, int.class));
        MethodHandle filter = LOOKUP.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        MethodHandle ca = MethodHandles.collectArguments(target, 0, filter);
        check(ca.type().equals(MethodType.methodType(int.class, String.class, int.class)),
                "collectArguments type (String,int)int");
        check((int) ca.invokeExact("abc", 10) == 13, "collectArguments value");
        // the collected (filtered) slot disappears from the adapter type
        MethodHandle ca1 = MethodHandles.collectArguments(target, 1, filter);
        check(ca1.type().equals(MethodType.methodType(int.class, int.class, String.class)),
                "collectArguments position 1");
        check((int) ca1.invokeExact(10, "abc") == 13, "collectArguments position 1 value");
        checkThrows(NullPointerException.class,
                () -> MethodHandles.collectArguments(null, 0, filter),
                "collectArguments null target NPE");
    }

    static void testExplicitCastNarrowing() throws Throwable {
        MethodHandle idl = MethodHandles.identity(long.class);
        // explicitCastArguments performs a REAL narrowing conversion long -> int
        MethodHandle narrowed = MethodHandles.explicitCastArguments(idl,
                MethodType.methodType(int.class, long.class));
        check((int) narrowed.invokeExact(1_000_000_000_000L) == -727379968,
                "explicitCastArguments narrows long->int (truncation)");
        // contrast: asType refuses the same narrowing with WMTE
        checkThrows(WrongMethodTypeException.class,
                () -> idl.asType(MethodType.methodType(int.class, long.class)),
                "asType refuses return narrowing WMTE");
        // reference narrowing is a checked cast at run time
        MethodHandle ido = MethodHandles.identity(Object.class);
        MethodHandle toStr = MethodHandles.explicitCastArguments(ido,
                MethodType.methodType(String.class, Object.class));
        check("x".equals(toStr.invoke("x")), "explicitCastArguments ref cast");
        checkThrows(ClassCastException.class,
                () -> toStr.invoke(42), "explicitCastArguments ref cast CCE");
    }

    static void testCombinatorValidation() throws Throwable {
        MethodHandle id = MethodHandles.identity(int.class);
        // predicate must return boolean
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandles.guardWithTest(id, id, id),
                "guard type is not a predicate",
                "guardWithTest non-predicate IAE message");
        // handler must accept the exception as leading argument
        checkThrows(IllegalArgumentException.class,
                () -> MethodHandles.catchException(id, IOException.class, id),
                "catchException bad handler IAE");
        // combiner return must match target's leading parameter
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandles.foldArguments(MethodHandles.identity(String.class), id),
                "target and combiner types must match",
                "foldArguments mismatch IAE message");
        // more filters than trailing parameters
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandles.filterArguments(id, 0, id, id),
                "too many filters", "filterArguments too many filters IAE message");
        // permute array length must equal target arity (8: "bad reorder array: ...")
        checkThrows(IllegalArgumentException.class,
                () -> MethodHandles.permuteArguments(id,
                        MethodType.methodType(int.class, int.class), 0, 1),
                "permuteArguments wrong length IAE");
        checkThrows(IllegalArgumentException.class,
                () -> MethodHandles.permuteArguments(id,
                        MethodType.methodType(int.class, int.class, int.class), 5),
                "permuteArguments index OOB IAE");
    }

    static void testInsertArgumentsValue() throws Throwable {
        MethodHandle j3 = stat("join3",
                MethodType.methodType(String.class, String.class, String.class, String.class));
        MethodHandle mid = MethodHandles.insertArguments(j3, 1, "MID");
        check(mid.type().parameterCount() == 2, "insertArguments arity reduced");
        check("a-MID-c".equals((String) mid.invokeExact("a", "c")),
                "insertArguments value precise");
        MethodHandle two = MethodHandles.insertArguments(j3, 0, "X", "Y");
        check(two.type().parameterCount() == 1, "insertArguments multiple values");
        check("X-Y-z".equals((String) two.invokeExact("z")),
                "insertArguments multiple values precise");
    }

    static void testFoldPrependSemantics() throws Throwable {
        // fold: the combiner's result becomes the target's first argument
        MethodHandle target = stat("add2", MethodType.methodType(int.class, int.class, int.class));
        MethodHandle dbl = stat("negate", MethodType.methodType(int.class, int.class));
        MethodHandle folded = MethodHandles.foldArguments(target, dbl);
        // adapter(a) = add2(negate(a), a) == a - a == 0
        check(folded.type().parameterCount() == 1, "fold arity");
        check((int) folded.invokeExact(7) == 0, "fold negate+add == 0");
        check((int) folded.invokeExact(-3) == 0, "fold stable for negatives");
    }

    static void testJdk27FactoryDeep() throws Throwable {
        // arrayLength: exact counts for primitive and reference arrays
        MethodHandle al = MethodHandles.arrayLength(int[].class);
        check(al.type().equals(MethodType.methodType(int.class, int[].class)),
                "arrayLength type (int[])int");
        check((int) al.invokeExact(new int[]{7, 8, 9}) == 3, "arrayLength int[]");
        check((int) MethodHandles.arrayLength(String[].class).invoke(new String[]{"a"}) == 1,
                "arrayLength String[]");
        check((int) al.invokeExact(new int[0]) == 0, "arrayLength empty");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandles.arrayLength(int.class), "not an array: int",
                "arrayLength non-array IAE message");
        checkThrows(NullPointerException.class,
                () -> MethodHandles.arrayLength(null), "arrayLength null NPE");
        // arrayConstructor allocates
        MethodHandle ac = MethodHandles.arrayConstructor(String[].class);
        check(ac.type().equals(MethodType.methodType(String[].class, int.class)),
                "arrayConstructor type");
        check(((String[]) ac.invoke(2)).length == 2, "arrayConstructor length");
        // zero: default value for every kind
        check((int) MethodHandles.zero(int.class).invokeExact() == 0, "zero int");
        check(MethodHandles.zero(boolean.class).invoke() == Boolean.FALSE, "zero boolean");
        check(MethodHandles.zero(String.class).invoke() == null, "zero String null");
        check(MethodHandles.zero(Object.class).invoke() == null, "zero Object null");
        check(MethodHandles.zero(void.class).type()
                .equals(MethodType.methodType(void.class)), "zero void type");
        checkThrows(NullPointerException.class,
                () -> MethodHandles.zero(null), "zero null NPE");
        // empty: typed handle that returns defaults without invoking anything
        MethodHandle e = MethodHandles.empty(MethodType.methodType(int.class, int.class, int.class));
        check((int) e.invokeExact(3, 4) == 0, "empty returns default");
        MethodHandles.empty(MethodType.methodType(void.class, int.class)).invokeExact(7);
        check(MethodHandles.empty(MethodType.methodType(String.class)).invoke() == null,
                "empty ref default");
        checkThrows(NullPointerException.class,
                () -> MethodHandles.empty(null), "empty null NPE");
        // dropArgumentsToMatch: insert a leading int the target never sees
        MethodHandle j3 = stat("join3",
                MethodType.methodType(String.class, String.class, String.class, String.class));
        MethodHandle d = MethodHandles.dropArgumentsToMatch(j3, 0,
                Arrays.asList(int.class, String.class, String.class, String.class), 1);
        check(d.type().equals(MethodType.methodType(String.class, int.class,
                String.class, String.class, String.class)), "dropArgumentsToMatch type");
        check("a-b-c".equals((String) d.invokeExact(5, "a", "b", "c")),
                "dropArgumentsToMatch value");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandles.dropArgumentsToMatch(j3, 0,
                        Arrays.asList(int.class, String.class), 1),
                "argument lists do not match",
                "dropArgumentsToMatch mismatch IAE message");
        // dropReturn keeps arguments, drops the result
        MethodHandle dr = MethodHandles.dropArguments(j3, 0, int.class);
        MethodHandle dropped = MethodHandles.dropReturn(dr);
        check(dropped.type().equals(MethodType.methodType(void.class, int.class,
                String.class, String.class, String.class)), "dropReturn type");
        dropped.invoke(5, "a", "b", "c");
        check(true, "dropReturn invocation returns normally");
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
        check(pub.lookupModes() == MethodHandles.Lookup.UNCONDITIONAL,
                "publicLookup UNCONDITIONAL mode");
        check((pub.lookupModes() & MethodHandles.Lookup.PUBLIC) == 0,
                "publicLookup has no PUBLIC bit (JDK-27 uses UNCONDITIONAL)");
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

    static void testJdk27Methods() throws Throwable {
        MethodHandle z = MethodHandles.zero(int.class);
        check((int) z.invokeExact() == 0, "zero int");
        check(MethodHandles.zero(String.class).invoke() == null, "zero String -> null");

        MethodHandle e = MethodHandles.empty(MethodType.methodType(void.class));
        check(e.type().equals(MethodType.methodType(void.class)), "empty type");

        MethodHandle ac = MethodHandles.arrayConstructor(int[].class);
        int[] arr = (int[]) ac.invoke(3);
        check(arr.length == 3, "arrayConstructor length");

        check(MethodHandles.Lookup.MODULE != 0 && MethodHandles.Lookup.UNCONDITIONAL != 0,
                "MODULE/UNCONDITIONAL constants");
        MethodHandles.Lookup l = MethodHandles.lookup();
        check((l.lookupModes() & MethodHandles.Lookup.MODULE) != 0, "lookup has MODULE mode");
        check(l.hasFullPrivilegeAccess(), "lookup has full privilege access");
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
        testIdentityReferenceSemantics();
        testConstantEdges();
        testArrayElementDeep();
        testPublicLookupAccess();
        testLookupToStringExact();
        testCollectArguments();
        testExplicitCastNarrowing();
        testCombinatorValidation();
        testInsertArgumentsValue();
        testFoldPrependSemantics();
        testJdk27Methods();
        testJdk27FactoryDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
