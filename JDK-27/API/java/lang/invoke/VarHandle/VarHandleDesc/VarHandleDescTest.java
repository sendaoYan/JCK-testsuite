/*
 * @test
 * @summary java.lang.invoke.VarHandle.VarHandleDesc conformance: white-box + black-box behavior.
 *          Deepened: exact toString formats for all three factories (incl. nested
 *          arrays), varType descriptor/displayName, equals/hashCode value semantics,
 *          describeConstable round-trip equality (field/static/array), resolved-handle
 *          behavioral equivalence (VarHandle has no equals), null-argument and
 *          resolution-failure contracts (CNFE/NSFE/IAE measured).
 * @run main VarHandleDescTest
 */

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.VarHandle.VarHandleDesc;

public class VarHandleDescTest {
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

    /** Negative-case owner: private class, unreachable from publicLookup. */
    static class Secret {
        private int hidden = 1;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.VarHandle$VarHandleDesc");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Class.forName("java.lang.constant.DynamicConstantDesc"),
              "extends DynamicConstantDesc<VarHandle>");
        int pubCtors = 0;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors()) {
            if (java.lang.reflect.Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor (factories only)");
        for (String n : new String[]{"ofField", "ofStaticField", "ofArray", "varType",
                "resolveConstantDesc", "toString"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        ClassDesc holder = ClassDesc.of("VarHandleDescTest").nested("Holder");

        // ofField: nominal form of an instance field VarHandle
        VarHandleDesc fd = VarHandleDesc.ofField(holder, "x", ConstantDescs.CD_int);
        check(fd.varType().descriptorString().equals("I"), "ofField varType");
        check(fd.toString().contains("x"), "toString names the field: " + fd);
        VarHandle resolved = fd.resolveConstantDesc(lookup);
        check(resolved.varType() == int.class, "resolved varType");
        check(resolved.coordinateTypes().equals(java.util.List.of(Holder.class)),
              "resolved coordinates");
        Holder h = new Holder();
        check((int) resolved.get(h) == 5, "resolved field VarHandle readable");
        resolved.set(h, 8);
        check(h.x == 8, "resolved field VarHandle writable");

        // equality with the desc produced by a live VarHandle
        VarHandle live = lookup.findVarHandle(Holder.class, "x", int.class);
        check(live.describeConstable().isPresent(), "live describeConstable present");
        check(fd.equals(live.describeConstable().orElseThrow()),
              "ofField matches describeConstable");

        // ofStaticField
        VarHandleDesc sd = VarHandleDesc.ofStaticField(
                ClassDesc.of("VarHandleDescTest"), "stat", ConstantDescs.CD_int);
        VarHandle sResolved = sd.resolveConstantDesc(lookup);
        check(sResolved.coordinateTypes().isEmpty(), "static resolved coordinates");
        check((int) sResolved.get() == 77, "static resolved readable");
        sResolved.set(78);
        check(stat == 78, "static resolved writable");

        // ofArray: nominal form of an array element VarHandle
        VarHandleDesc ad = VarHandleDesc.ofArray(ClassDesc.of("java.lang.String").arrayType());
        check(ad.varType().descriptorString().equals("Ljava/lang/String;"), "ofArray varType");
        VarHandle aResolved = ad.resolveConstantDesc(lookup);
        String[] strs = {"a", "b"};
        check("b".equals((String) aResolved.get(strs, 1)), "array resolved readable");
        aResolved.set(strs, 1, "z");
        check(strs[1].equals("z"), "array resolved writable");
        check(aResolved.coordinateTypes().equals(java.util.List.of(String[].class, int.class)),
              "array resolved coordinates");

        // value semantics
        check(fd.equals(VarHandleDesc.ofField(holder, "x", ConstantDescs.CD_int)),
              "equals by value");
        check(!fd.equals(VarHandleDesc.ofField(holder, "y", ConstantDescs.CD_int)),
              "field name participates");
        check(!fd.equals(sd), "!equals other factory");
    }

    // ---- Part 3: deepened contract coverage ----

    static void testToStringAndVarType() {
        ClassDesc holder = ClassDesc.of("VarHandleDescTest").nested("Holder");
        VarHandleDesc fd = VarHandleDesc.ofField(holder, "x", ConstantDescs.CD_int);
        check(fd.toString().equals("VarHandleDesc[VarHandleDescTest$Holder.x:int]"),
                "ofField toString exact");
        check(fd.varType().displayName().equals("int"), "ofField varType displayName");
        check(fd.varType().descriptorString().equals("I"), "ofField varType descriptor");
        VarHandleDesc sd = VarHandleDesc.ofStaticField(
                ClassDesc.of("VarHandleDescTest"), "stat", ConstantDescs.CD_int);
        check(sd.toString().equals("VarHandleDesc[static VarHandleDescTest.stat:int]"),
                "ofStaticField toString exact");
        check(sd.varType().descriptorString().equals("I"), "ofStaticField varType descriptor");
        VarHandleDesc ad = VarHandleDesc.ofArray(ClassDesc.of("java.lang.String").arrayType());
        check(ad.toString().equals("VarHandleDesc[String[][]]"), "ofArray toString exact");
        check(ad.varType().descriptorString().equals("Ljava/lang/String;"),
                "ofArray varType is element type");
        VarHandleDesc id = VarHandleDesc.ofArray(ConstantDescs.CD_int.arrayType());
        check(id.toString().equals("VarHandleDesc[int[][]]"), "int[] ofArray toString exact");
        check(id.varType().descriptorString().equals("I"), "int[] ofArray varType");
    }

    static void testNestedArrayDesc() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        // int[][]: the desc's varType is the element type int[]; coordinates gain a row
        VarHandleDesc dd = VarHandleDesc.ofArray(ConstantDescs.CD_int.arrayType().arrayType());
        check(dd.toString().equals("VarHandleDesc[int[][][]]"), "nested array toString exact");
        check(dd.varType().descriptorString().equals("[I"), "nested array varType is element type");
        VarHandle dvh = dd.resolveConstantDesc(lookup);
        check(dvh.coordinateTypes().equals(java.util.List.of(int[][].class, int.class)),
                "nested array coordinates");
        check(dvh.varType() == int[].class, "nested array varType class");
        int[][] m = {{1, 2}, {3, 4}};
        dvh.set(m, 1, new int[]{9, 9});
        check(m[1][0] == 9, "nested array set");
        check(((int[]) dvh.get(m, 0))[1] == 2, "nested array get");
    }

    static void testEqualitySemantics() {
        ClassDesc holder = ClassDesc.of("VarHandleDescTest").nested("Holder");
        VarHandleDesc fd = VarHandleDesc.ofField(holder, "x", ConstantDescs.CD_int);
        VarHandleDesc fd2 = VarHandleDesc.ofField(holder, "x", ConstantDescs.CD_int);
        check(fd.equals(fd2) && fd2.equals(fd), "equals symmetric");
        check(fd.hashCode() == fd2.hashCode(), "hashCode consistent with equals");
        check(!fd.equals(null), "equals null is false");
        check(!fd.equals("not a desc"), "equals foreign type is false");
        check(!fd.equals(VarHandleDesc.ofField(holder, "y", ConstantDescs.CD_int)),
                "field name participates");
        check(!fd.equals(VarHandleDesc.ofField(holder, "x", ConstantDescs.CD_long)),
                "var type participates");
        check(!fd.equals(VarHandleDesc.ofField(ClassDesc.of("VarHandleDescTest"), "x",
                ConstantDescs.CD_int)), "declaring class participates");
        check(!fd.equals(VarHandleDesc.ofArray(ConstantDescs.CD_int.arrayType())),
                "field desc != array desc");
        check(!fd.equals(VarHandleDesc.ofStaticField(holder, "x", ConstantDescs.CD_int)),
                "instance desc != static desc");
        check(fd instanceof java.lang.constant.ConstantDesc, "VarHandleDesc is a ConstantDesc");
    }

    static void testDescribeConstableRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        VarHandle live = lookup.findVarHandle(Holder.class, "x", int.class);
        VarHandleDesc fd = live.describeConstable().orElseThrow();
        check(fd.equals(VarHandleDesc.ofField(
                ClassDesc.of("VarHandleDescTest").nested("Holder"), "x", ConstantDescs.CD_int)),
                "live field desc equals ofField construct");
        VarHandle r1 = fd.resolveConstantDesc(lookup);
        VarHandle r2 = fd.resolveConstantDesc(lookup);
        check(r1 != r2, "each resolve yields a fresh handle");
        check(!r1.equals(r2) && !r1.equals(live), "VarHandle defines no equals");
        check(r1.toString().equals(live.toString()), "resolved toString equals live");
        check(r1.accessModeType(VarHandle.AccessMode.GET)
                      .equals(live.accessModeType(VarHandle.AccessMode.GET)),
                "resolved accessModeType equals live");
        check(r1.varType() == live.varType() && r1.coordinateTypes().equals(live.coordinateTypes()),
                "resolved shape equals live");
        Holder h = new Holder();
        r1.set(h, 11);
        check((int) r2.get(h) == 11, "two resolved handles observe the same field");
        VarHandle svh = lookup.findStaticVarHandle(VarHandleDescTest.class, "stat", int.class);
        check(svh.describeConstable().orElseThrow().equals(VarHandleDesc.ofStaticField(
                ClassDesc.of("VarHandleDescTest"), "stat", ConstantDescs.CD_int)),
                "live static desc equals ofStaticField construct");
        VarHandle avh = MethodHandles.arrayElementVarHandle(String[].class);
        check(avh.describeConstable().orElseThrow().equals(
                VarHandleDesc.ofArray(ClassDesc.of("java.lang.String").arrayType())),
                "live array desc equals ofArray construct");
    }

    static void testNegative() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        ClassDesc holder = ClassDesc.of("VarHandleDescTest").nested("Holder");
        checkThrows(NullPointerException.class,
                () -> VarHandleDesc.ofField(null, "x", ConstantDescs.CD_int),
                "ofField null declaring class");
        checkThrows(NullPointerException.class,
                () -> VarHandleDesc.ofField(holder, null, ConstantDescs.CD_int),
                "ofField null field name");
        checkThrows(NullPointerException.class,
                () -> VarHandleDesc.ofField(holder, "x", null), "ofField null var type");
        checkThrows(NullPointerException.class,
                () -> VarHandleDesc.ofStaticField(null, "stat", ConstantDescs.CD_int),
                "ofStaticField null class");
        checkThrows(NullPointerException.class,
                () -> VarHandleDesc.ofArray(null), "ofArray null class");
        checkThrowsMsg(ClassNotFoundException.class, "no.such.Clazz",
                () -> VarHandleDesc.ofField(ClassDesc.of("no.such.Clazz"), "f", ConstantDescs.CD_int)
                        .resolveConstantDesc(lookup), "resolve unknown class");
        checkThrowsMsg(NoSuchFieldException.class, "no such field",
                () -> VarHandleDesc.ofField(holder, "nope", ConstantDescs.CD_int)
                        .resolveConstantDesc(lookup), "resolve unknown field");
        checkThrowsMsg(NoSuchFieldException.class, "Holder.x/long",
                () -> VarHandleDesc.ofField(holder, "x", ConstantDescs.CD_long)
                        .resolveConstantDesc(lookup), "resolve wrong var type");
        checkThrowsMsg(IllegalArgumentException.class, "not an array",
                () -> VarHandleDesc.ofArray(ConstantDescs.CD_int).resolveConstantDesc(lookup),
                "ofArray rejects non-array class");
        // publicLookup resolves through java.lang.Object's (bootstrap) loader: app classes
        // are invisible -> ClassNotFoundException (measured)
        checkThrowsMsg(ClassNotFoundException.class, "Secret",
                () -> VarHandleDesc.ofField(
                        ClassDesc.of("VarHandleDescTest").nested("Secret"), "hidden",
                        ConstantDescs.CD_int).resolveConstantDesc(MethodHandles.publicLookup()),
                "publicLookup cannot resolve app-class owner");
    }

    static int stat = 77;

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testToStringAndVarType();
        testNestedArrayDesc();
        testEqualitySemantics();
        testDescribeConstableRoundTrip();
        testNegative();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
