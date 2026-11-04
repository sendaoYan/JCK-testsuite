/*
 * @test
 * @summary java.lang.invoke.MethodHandle conformance: full contract coverage.
 *          type() signature, invokeExact exact-type dispatch + WrongMethodTypeException
 *          on mismatched argument/return types, invoke's permissive adaptation
 *          (boxing/unboxing/widening), invokeWithArguments (varargs + List forms) and
 *          arity mismatch, asType adaptation, bindTo receiver binding and arity reduction,
 *          asCollector/asSpreader array adaptation, asVarargsCollector/isVarargsCollector/
 *          asFixedArity round-trips, void-returning invoke yields null, and toString.
 *          Deepened: (Object)invokeExact return-cast WMTE + asType-then-exact, asType
 *          conversion matrix (return widening OK / param widening + narrowing WMTE
 *          with exact "cannot convert" messages), call-site widening via invoke,
 *          invokeWithArguments CCE/NPE paths, combinator value semantics
 *          (filterArguments multi/positional, foldArguments prepend, insertArguments
 *          binding, dropArguments passthrough, permuteArguments rotate/duplicate,
 *          guardWithTest branch counters, catchException identity/mismatch
 *          propagation), collector+spreader round-trip, bindTo wrong-type CCE,
 *          toString "MethodHandle"+type exact.
 * @run main MethodHandleTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.WrongMethodTypeException;
import java.util.Arrays;

public class MethodHandleTest {
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

    static class Calc {
        public int add(int a, int b) { return a + b; }
        public static int sum(int... xs) { int t = 0; for (int x : xs) t += x; return t; }
        public void nop() { }
    }

    static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    static MethodHandle add() throws Exception {
        return LOOKUP.findVirtual(Calc.class, "add", MethodType.methodType(int.class, int.class, int.class));
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

    // --- deepening targets below ---
    static class Cat {
        public static String concat(String a, String b) { return a + b; }
        public static String tag(String s) { return s + "!"; }
        public static String cat3(String a, String b, String c) { return a + "|" + b + "|" + c; }
    }

    static class Thrower {
        static int checked(int x) throws java.io.IOException {
            if (x < 0) throw new java.io.IOException("neg " + x);
            return x;
        }
        static int runtime(int x) {
            if (x < 0) throw new IllegalStateException("rt " + x);
            return x;
        }
        static int recover(java.io.IOException e, int x) { return -1; }
        static int recoverRuntime(IllegalStateException e, int x) { return -2; }
        static int recoverIAE(IllegalArgumentException e, int x) { return -3; }
    }

    static int trueBranch, falseBranch;

    static boolean positive(int x) { if (x >= 0) trueBranch++; else falseBranch++; return x >= 0; }

    static int negateInt(int x) { return -x; }

    static void testInvokeExactReturnContract() throws Throwable {
        // a (String)String handle invoked through an (Object)Object call site is a
        // signature mismatch -> WrongMethodTypeException (both releases, wording differs:
        // 8: "expected (Calc,String)String but found ...", 27: "handle's method type ... but found ...")
        MethodHandle tag = LOOKUP.findStatic(Cat.class, "tag",
                MethodType.methodType(String.class, String.class));
        check("ab!".equals((String) tag.invokeExact("ab")), "invokeExact exact String");
        checkThrowsMsg(WrongMethodTypeException.class,
                () -> { Object r = (Object) tag.invokeExact("ab"); },
                "but found", "(Object)invokeExact on String handle WMTE");
        // adapt first, then the same call site type works
        MethodHandle adapted = tag.asType(MethodType.methodType(Object.class, Object.class));
        check("ab!".equals((Object) adapted.invokeExact((Object) "ab")),
                "asType'd invokeExact Object site");
        // primitive return mismatch is also WMTE, not ClassCastException
        MethodHandle len = MethodHandles.identity(int.class);
        checkThrows(WrongMethodTypeException.class,
                () -> { long r = (long) len.invokeExact(7); },
                "(long)invokeExact on int handle WMTE");
        check((int) len.invokeExact(7) == 7, "(int)invokeExact exact");
    }

    static void testAsTypeConversions() throws Throwable {
        MethodHandle add2 = LOOKUP.findStatic(Cat.class, "concat",
                MethodType.methodType(String.class, String.class, String.class));
        // reference widening/casting in return and params is fine
        MethodHandle widened = add2.asType(MethodType.methodType(Object.class,
                Object.class, Object.class));
        check("ab".equals(widened.invoke("a", "b")), "asType Object/OO");
        // primitive RETURN widening (int -> long) is a legal asType conversion
        MethodHandle idInt = MethodHandles.identity(int.class);
        MethodHandle widenedRet = idInt.asType(MethodType.methodType(long.class, int.class));
        check((long) widenedRet.invokeExact(5) == 5L, "asType return widening int->long");
        // PARAMETER widening (int -> long) is NOT an asType conversion: WMTE at adaptation time
        checkThrowsMsg(WrongMethodTypeException.class,
                () -> idInt.asType(MethodType.methodType(int.class, long.class)),
                "cannot convert MethodHandle(int)int to (long)int",
                "asType param widening WMTE");
        // return narrowing (long -> int) likewise rejected
        MethodHandle idLong = MethodHandles.identity(long.class);
        checkThrowsMsg(WrongMethodTypeException.class,
                () -> idLong.asType(MethodType.methodType(int.class, long.class)),
                "cannot convert MethodHandle(long)long to (long)int",
                "asType return narrowing WMTE");
        // arity must match exactly
        checkThrowsMsg(WrongMethodTypeException.class,
                () -> add2.asType(MethodType.methodType(String.class, String.class)),
                "cannot convert MethodHandle(String,String)String to (String)String",
                "asType arity WMTE");
        // asType to the same type returns the same handle (cached identity)
        check(idInt.asType(idInt.type()) == idInt, "asType same type is identity");
    }

    static void testInvokeCallSiteWidening() throws Throwable {
        // invoke() applies method-invocation conversion at the call site:
        // int argument feeding a long parameter
        MethodHandle lsub = MethodHandles.identity(long.class);
        check((long) lsub.invoke(7) == 7L, "invoke int-site on (long)long");
        check((long) lsub.invokeExact(7L) == 7L, "invokeExact long site");
        // widening + boxing in one call
        MethodHandle add = add();
        Calc c = new Calc();
        check((int) add.invoke(c, (byte) 2, (short) 3) == 5, "invoke byte/short widening");
    }

    static void testInvokeWithArgumentsTypes() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        // scattered and List forms are equivalent
        check(mh.invokeWithArguments(c, 2, 3).equals(mh.invokeWithArguments(Arrays.asList(c, 2, 3))),
                "iwa scattered == List");
        // too many arguments: WMTE naming the attempted generic type
        checkThrowsMsg(WrongMethodTypeException.class,
                () -> mh.invokeWithArguments(c, 1, 2, 3),
                "cannot convert MethodHandle(Calc,int,int)int to (Object,Object,Object,Object)Object",
                "iwa too many WMTE message");
        // an argument that cannot be converted -> ClassCastException (not WMTE)
        checkThrowsMsg(ClassCastException.class,
                () -> mh.invokeWithArguments(c, "a", "b"),
                "cannot be cast to",
                "iwa unconvertible arg CCE");
        // null argument to a primitive parameter -> NPE during unboxing
        checkThrows(NullPointerException.class,
                () -> mh.invokeWithArguments(c, null, 1),
                "iwa null primitive arg NPE");
    }

    static void testFilterArgumentsMulti() throws Throwable {
        MethodHandle concat = LOOKUP.findStatic(Cat.class, "concat",
                MethodType.methodType(String.class, String.class, String.class));
        MethodHandle tag = LOOKUP.findStatic(Cat.class, "tag",
                MethodType.methodType(String.class, String.class));
        // each of the two parameters gets its own filter
        MethodHandle filtered = MethodHandles.filterArguments(concat, 0, tag, tag);
        check("a!b!".equals((String) filtered.invokeExact("a", "b")),
                "filterArguments two filters");
        // filter only the second parameter
        MethodHandle filtered1 = MethodHandles.filterArguments(concat, 1, tag);
        check("ab!".equals((String) filtered1.invokeExact("a", "b")),
                "filterArguments position 1");
        // a filter that narrows the incoming type seen by the target (int -> String via length)
        MethodHandle len = LOOKUP.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        MethodHandle sumLen = LOOKUP.findStatic(Calc.class, "sum",
                MethodType.methodType(int.class, int[].class)).asFixedArity()
                .asCollector(int[].class, 2);
        // sumLen: (int,int)int; filter both params with length: (String,String)int
        MethodHandle lengthSum = MethodHandles.filterArguments(sumLen, 0, len, len);
        check(lengthSum.type().equals(MethodType.methodType(int.class, String.class, String.class)),
                "filterArguments type-changing filter type");
        check((int) lengthSum.invokeExact("abc", "de") == 5,
                "filterArguments type-changing filter value");
        // too many filters for the trailing parameters -> IAE
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandles.filterArguments(len, 0, tag, tag),
                "too many filters", "filterArguments too many filters IAE");
    }

    static void testFoldArgumentsValue() throws Throwable {
        MethodHandle concat = LOOKUP.findStatic(Cat.class, "concat",
                MethodType.methodType(String.class, String.class, String.class));
        MethodHandle tag = LOOKUP.findStatic(Cat.class, "tag",
                MethodType.methodType(String.class, String.class));
        // fold: combiner(s)=tag(s) becomes the FIRST argument: concat(tag(s), s)
        MethodHandle folded = MethodHandles.foldArguments(concat, tag);
        check(folded.type().parameterCount() == 1, "foldArguments arity 1");
        check("a!a".equals((String) folded.invokeExact("a")), "foldArguments value");
        // mismatched combiner -> IAE
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandles.foldArguments(MethodHandles.identity(String.class),
                        MethodHandles.identity(int.class)),
                "target and combiner types must match",
                "foldArguments mismatch IAE");
    }

    static void testInsertDropArgumentsValue() throws Throwable {
        MethodHandle cat3 = LOOKUP.findStatic(Cat.class, "cat3",
                MethodType.methodType(String.class, String.class, String.class, String.class));
        MethodHandle bound = MethodHandles.insertArguments(cat3, 1, "MID");
        check(bound.type().parameterCount() == 2, "insertArguments arity");
        check("a|MID|c".equals((String) bound.invokeExact("a", "c")),
                "insertArguments bound value");
        // dropping leading and trailing args keeps the middle passing through
        MethodHandle dropFront = MethodHandles.dropArguments(cat3, 0, int.class);
        check("a|b|c".equals((String) dropFront.invokeExact(9, "a", "b", "c")),
                "dropArguments front passthrough");
        MethodHandle dropBoth = MethodHandles.dropArguments(
                MethodHandles.dropArguments(cat3, 3, long.class), 0, int.class);
        check("a|b|c".equals((String) dropBoth.invokeExact(1, "a", "b", "c", 2L)),
                "dropArguments front+back passthrough");
        // out-of-range position -> IAE (message differs slightly across releases)
        checkThrows(IllegalArgumentException.class,
                () -> MethodHandles.insertArguments(cat3, 9, "x"),
                "insertArguments OOB IAE");
        checkThrows(IllegalArgumentException.class,
                () -> MethodHandles.dropArguments(cat3, 9, int.class),
                "dropArguments OOB IAE");
        // wrong value type -> CCE at combinator build time
        checkThrowsMsg(ClassCastException.class,
                () -> MethodHandles.insertArguments(cat3, 0, 42),
                "Cannot cast java.lang.Integer to java.lang.String",
                "insertArguments wrong value CCE");
    }

    static void testPermuteArgumentsValue() throws Throwable {
        MethodHandle cat3 = LOOKUP.findStatic(Cat.class, "cat3",
                MethodType.methodType(String.class, String.class, String.class, String.class));
        // reorder (a,b,c) -> call with (c,a,b): new param i maps per perm[i]=source
        MethodHandle rot = MethodHandles.permuteArguments(cat3,
                MethodType.methodType(String.class, String.class, String.class, String.class),
                2, 0, 1);
        check("c|a|b".equals((String) rot.invokeExact("a", "b", "c")),
                "permuteArguments rotate");
        // duplicate: both target params fed from new param 0
        MethodHandle dup = MethodHandles.permuteArguments(cat3,
                MethodType.methodType(String.class, String.class, String.class, String.class),
                0, 0, 0);
        check("a|a|a".equals((String) dup.invokeExact("a", "b", "c")),
                "permuteArguments duplicate index");
        // wrong array length vs target arity -> IAE (8: "bad reorder array: [0, 1]")
        checkThrows(IllegalArgumentException.class,
                () -> MethodHandles.permuteArguments(cat3,
                        MethodType.methodType(String.class, String.class, String.class),
                        0, 1),
                "permuteArguments wrong array length IAE");
        // index out of range for the new type -> IAE
        checkThrows(IllegalArgumentException.class,
                () -> MethodHandles.permuteArguments(cat3,
                        MethodType.methodType(String.class, String.class, String.class,
                                String.class, String.class),
                        0, 1, 2, 5),
                "permuteArguments index OOB IAE");
    }

    static void testGuardWithTestBranches() throws Throwable {
        MethodHandle pred = LOOKUP.findStatic(MethodHandleTest.class, "positive",
                MethodType.methodType(boolean.class, int.class));
        MethodHandle id = MethodHandles.identity(int.class);
        MethodHandle neg = LOOKUP.findStatic(MethodHandleTest.class, "negateInt",
                MethodType.methodType(int.class, int.class));
        MethodHandle guard = MethodHandles.guardWithTest(pred, id, neg);
        check(guard.type().equals(MethodType.methodType(int.class, int.class)),
                "guardWithTest type matches target");
        trueBranch = 0; falseBranch = 0;
        check((int) guard.invoke(5) == 5, "guard true branch value");
        check((int) guard.invoke(-5) == 5, "guard false branch value (negated)");
        check(trueBranch == 1 && falseBranch == 1, "guardWithTest each branch ran once");
        // a non-boolean predicate is rejected
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandles.guardWithTest(id, id, id),
                "guard type is not a predicate",
                "guardWithTest non-predicate IAE");
    }

    static void testCatchExceptionPrecise() throws Throwable {
        MethodHandle target = LOOKUP.findStatic(Thrower.class, "checked",
                MethodType.methodType(int.class, int.class));
        MethodHandle handler = LOOKUP.findStatic(Thrower.class, "recover",
                MethodType.methodType(int.class, java.io.IOException.class, int.class));
        MethodHandle guarded = MethodHandles.catchException(target, java.io.IOException.class, handler);
        check((int) guarded.invokeExact(5) == 5, "catchException normal path");
        check((int) guarded.invokeExact(-5) == -1, "catchException handler value");
        // runtime exception of a DIFFERENT type than the caught class propagates unchanged
        MethodHandle rt = LOOKUP.findStatic(Thrower.class, "runtime",
                MethodType.methodType(int.class, int.class));
        MethodHandle rtGuard = MethodHandles.catchException(rt,
                IllegalArgumentException.class,
                LOOKUP.findStatic(Thrower.class, "recoverIAE",
                        MethodType.methodType(int.class, IllegalArgumentException.class, int.class)));
        try {
            int r = (int) rtGuard.invokeExact(-1);
            throw new AssertionError("expected IllegalStateException to propagate but got " + r);
        } catch (IllegalStateException e) {
            check("rt -1".equals(e.getMessage()), "catchException mismatched type propagates");
        }
        // handler signature not accepting the exception -> IAE
        checkThrowsMsg(IllegalArgumentException.class,
                () -> MethodHandles.catchException(target, java.io.IOException.class,
                        MethodHandles.identity(int.class)),
                "handler does not accept exception type",
                "catchException bad handler IAE");
    }

    static void testBindToAndToString() throws Throwable {
        MethodHandle mh = add();
        // binding a receiver of the wrong type -> CCE at bindTo time
        checkThrowsMsg(ClassCastException.class,
                () -> mh.bindTo("not a Calc"),
                "Cannot cast java.lang.String",
                "bindTo wrong type CCE");
        // bindTo(null) is accepted; the failure surfaces at invocation
        MethodHandle nullBound = mh.bindTo(null);
        checkThrows(NullPointerException.class,
                () -> nullBound.invoke(2, 3),
                "null-bound receiver NPE at invoke");
        // toString is "MethodHandle" + type (no member name on either release)
        MethodHandle id = MethodHandles.identity(int.class);
        check(id.toString().equals("MethodHandle" + id.type().toString()),
                "toString equals MethodHandle+type");
        check(mh.toString().equals("MethodHandle(Calc,int,int)int"),
                "toString exact for findVirtual handle");
        check(mh.type().equals(MethodType.methodType(int.class, Calc.class, int.class, int.class)),
                "type() exact");
    }

    static void testCollectorSpreaderRoundTrip() throws Throwable {
        MethodHandle sum = LOOKUP.findStatic(Calc.class, "sum",
                MethodType.methodType(int.class, int[].class));
        // spread the collected args back out: collector then spreader restores the
        // fixed-arity shape with identical behavior
        MethodHandle roundTrip = sum.asCollector(int[].class, 3).asSpreader(int[].class, 3);
        check(roundTrip.type().equals(sum.type()), "collector+spreader restores type");
        check((int) roundTrip.invoke(new int[]{4, 5, 6}) == 15, "collector+spreader value");
        // negative collector length -> IAE with exact message
        checkThrowsMsg(IllegalArgumentException.class,
                () -> sum.asCollector(int[].class, -1),
                "array length is not legal: -1",
                "asCollector negative length IAE message");
        // spreading across a non-trailing position is rejected
        checkThrows(IllegalArgumentException.class,
                () -> add().asSpreader(int[].class, 4),
                "asSpreader length exceeds arity IAE");
    }

    static void testType() throws Exception {
        MethodHandle mh = add();
        MethodType t = mh.type();
        check(t.returnType() == int.class, "type returnType");
        // findVirtual prepends the receiver, so the handle type has 3 parameters
        check(t.parameterCount() == 3, "type parameterCount");
        check(t.parameterType(0) == Calc.class && t.parameterType(1) == int.class
                && t.parameterType(2) == int.class, "type parameter types");
    }

    static void testInvokeExact() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        int r = (int) mh.invokeExact(c, 2, 3);
        check(r == 5, "invokeExact exact");
        check((int) mh.invokeExact(c, -1, 1) == 0, "invokeExact exact zero");

        // wrong argument types -> WrongMethodTypeException
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeExact(c, (Integer) 2, (Integer) 3),
                "invokeExact boxed args WMTE");
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeExact(c, 2L, 3), "invokeExact long arg WMTE");
        // wrong arity -> WrongMethodTypeException
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeExact(c, 2), "invokeExact wrong arity WMTE");
        // null receiver where Calc expected -> WMTE (not NPE, because signature mismatch)
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeExact((Object) null, 2, 3), "invokeExact null receiver WMTE");
    }

    static void testInvoke() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        check((int) mh.invoke(c, 2, 3) == 5, "invoke");
        // invoke permits boxing/unboxing adaptation
        check((int) mh.invoke(c, Integer.valueOf(2), Integer.valueOf(3)) == 5,
                "invoke boxed adaptation");
        // invoke permits widening
        check((int) mh.invoke(c, (byte) 2, (short) 3) == 5, "invoke widening");
        // wrong arity -> WrongMethodTypeException
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invoke(c, 2), "invoke wrong arity WMTE");
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invoke(c, 2, 3, 4), "invoke too many WMTE");
        // String cannot be adapted to int -> WrongMethodTypeException
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invoke(c, "a", "b"), "invoke unadaptable type WMTE");
    }

    static void testInvokeWithArguments() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        check(mh.invokeWithArguments(c, 2, 3).equals(5), "invokeWithArguments varargs");
        check(mh.invokeWithArguments(Arrays.asList(c, 2, 3)).equals(5),
                "invokeWithArguments List");
        check(mh.invokeWithArguments(Arrays.<Object>asList(c, 4, 5)).equals(9),
                "invokeWithArguments List boxed");
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeWithArguments(c, 2), "invokeWithArguments wrong arity");
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeWithArguments(Arrays.asList(c, 2)),
                "invokeWithArguments List wrong arity");
        // null array is treated as zero arguments -> arity mismatch
        checkThrows(WrongMethodTypeException.class,
                () -> mh.invokeWithArguments((Object[]) null),
                "invokeWithArguments null array WMTE");
        checkThrows(NullPointerException.class,
                () -> mh.invokeWithArguments((java.util.List<?>) null),
                "invokeWithArguments null List NPE");
    }

    static void testAsType() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        // widen all three params and return to Object
        MethodHandle adapted = mh.asType(MethodType.methodType(Object.class,
                Object.class, Object.class, Object.class));
        check(adapted.type().returnType() == Object.class, "asType return type");
        check(adapted.invoke(c, 2, 3).equals(5), "asType adapted invoke");

        // box the two int parameters while keeping the Calc receiver
        MethodHandle boxed = mh.asType(MethodType.methodType(int.class,
                Calc.class, Integer.class, Integer.class));
        check((int) boxed.invoke(c, Integer.valueOf(2), Integer.valueOf(3)) == 5,
                "asType boxed params");
    }

    static void testBindTo() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        MethodHandle bound = mh.bindTo(c);
        check(bound.type().parameterCount() == 2, "bindTo does not reduce arity for instance");
        // bindTo on an instance method binds the receiver but leaves an explicit first arg
        // (this handle was obtained via findVirtual, so bindTo fixes the receiver)
        check((int) bound.invokeWithArguments(2, 3) == 5, "bindTo receiver invocation");
        check((int) bound.invoke(2, 3) == 5, "bindTo receiver invoke");

        // binding a static method's first arg reduces arity
        MethodHandle len = LOOKUP.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        MethodHandle lenBound = len.bindTo("abcde");
        check(lenBound.type().parameterCount() == 0, "bindTo static? no — virtual keeps arity");
        check((int) lenBound.invoke() == 5, "bound String.length");
    }

    static void testAsCollector() throws Throwable {
        MethodHandle sum = LOOKUP.findStatic(Calc.class, "sum",
                MethodType.methodType(int.class, int[].class));
        // collect two trailing int args into an int[] -> (int,int)int
        MethodHandle collected = sum.asCollector(int[].class, 2);
        check(collected.type().parameterCount() == 2
                && collected.type().parameterType(0) == int.class
                && collected.type().parameterType(1) == int.class, "asCollector type");
        check((int) collected.invoke(2, 3) == 5, "asCollector invoke");

        // zero-length collector -> ()int (empty array)
        MethodHandle none = sum.asCollector(int[].class, 0);
        check(none.type().parameterCount() == 0 && (int) none.invoke() == 0,
                "asCollector zero length");

        checkThrows(IllegalArgumentException.class,
                () -> sum.asCollector(String.class, 1), "asCollector bad array type IAE");
        checkThrows(IllegalArgumentException.class,
                () -> sum.asCollector(int[].class, -1), "asCollector negative length IAE");
        checkThrows(NullPointerException.class,
                () -> sum.asCollector(null, 2), "asCollector null arrayType NPE");
    }

    static void testAsSpreader() throws Throwable {
        MethodHandle mh = add();
        Calc c = new Calc();
        // spread an int[] over the two trailing int params -> (Calc,int[])int
        MethodHandle spread = mh.asSpreader(int[].class, 2);
        check(spread.type().parameterCount() == 2, "asSpreader parameter count");
        check(spread.type().parameterType(0) == Calc.class
                && spread.type().parameterType(1) == int[].class, "asSpreader param types");
        check((int) spread.invoke(c, new int[]{2, 3}) == 5, "asSpreader invoke");

        checkThrows(IllegalArgumentException.class,
                () -> mh.asSpreader(int[].class, -1), "asSpreader negative length IAE");
        checkThrows(IllegalArgumentException.class,
                () -> mh.asSpreader(int[].class, 4), "asSpreader length exceeds arity IAE");
        checkThrows(IllegalArgumentException.class,
                () -> mh.asSpreader(String.class, 1), "asSpreader non-array type IAE");
        checkThrows(NullPointerException.class,
                () -> mh.asSpreader(null, 2), "asSpreader null arrayType NPE");
    }

    static void testVarargsCollector() throws Throwable {
        MethodHandle sum = LOOKUP.findStatic(Calc.class, "sum",
                MethodType.methodType(int.class, int[].class));
        // findStatic on a varargs method yields a varargs collector by default
        check(sum.isVarargsCollector(), "varargs method handle is varargs collector");
        check((int) sum.invokeWithArguments(1, 2, 3) == 6, "varargs invokeWithArguments");

        MethodHandle var = sum.asVarargsCollector(int[].class);
        check(var.isVarargsCollector(), "asVarargsCollector keeps varargs");
        check((int) var.invokeWithArguments(4, 5) == 9, "asVarargsCollector invoke");

        MethodHandle fixed = sum.asFixedArity();
        check(!fixed.isVarargsCollector(), "asFixedArity clears varargs");
        check(fixed.type().equals(sum.type()), "asFixedArity restores type");
        check((int) fixed.invoke(new int[]{1, 2, 3}) == 6, "fixed arity invoke with array");
    }

    static void testVoidInvoke() throws Throwable {
        MethodHandle nop = LOOKUP.findVirtual(Calc.class, "nop", MethodType.methodType(void.class));
        Calc c = new Calc();
        Object r = nop.invoke(c);
        check(r == null, "void-returning invoke returns null");
        check(nop.toString() != null, "toString non-null");
    }

    public static void main(String[] args) throws Throwable {
        testType();
        testInvokeExact();
        testInvoke();
        testInvokeWithArguments();
        testAsType();
        testBindTo();
        testAsCollector();
        testAsSpreader();
        testVarargsCollector();
        testVoidInvoke();
        testInvokeExactReturnContract();
        testAsTypeConversions();
        testInvokeCallSiteWidening();
        testInvokeWithArgumentsTypes();
        testFilterArgumentsMulti();
        testFoldArgumentsValue();
        testInsertDropArgumentsValue();
        testPermuteArgumentsValue();
        testGuardWithTestBranches();
        testCatchExceptionPrecise();
        testBindToAndToString();
        testCollectorSpreaderRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
