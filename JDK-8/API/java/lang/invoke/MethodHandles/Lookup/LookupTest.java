/*
 * @test
 * @summary java.lang.invoke.MethodHandles.Lookup conformance: find* and unreflect,
 *          plus deepened access-control contracts: findVirtual/findStatic with
 *          missing-member NSME and private-member IAE ("no such method" wording),
 *          findGetter/findSetter + findStaticGetter/Setter round-trips (field write
 *          visible to plain reads), static field via findGetter -> IAE
 *          ("expected a non-static field"), final field setter -> IAE
 *          ("unexpected set of a final field"), findSpecial superclass dispatch,
 *          findConstructor round-trip + private ctor IAE, unreflect family
 *          (private Method -> IAE "member is private", setAccessible bypass,
 *          static-final setter IAE), in() downgrade semantics and toString forms.
 *          JDK-8 notes: no nests, so private members of nested target classes are
 *          NOT reachable (unlike JDK-11+); privateLookupIn/dropLookupMode/
 *          hasPrivateAccess/findClass/accessClass do not exist (locked by test).
 * @run main LookupTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class LookupTest {
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

    // ---- self-made target classes ----
    public static class Sample {
        public int pub = 1;
        public static int stat = 3;
        public static final int CONST = 7;
        public final int fin = 9;
        public Sample() { }
        private Sample(int x) { pub = x; }
        public int size() { return 42; }
        private int hidden() { return 13; }
        public static int stat() { return 55; }
        private static int hiddenStat() { return 66; }
    }

    public static class Sup { public int v() { return 1; } }
    public static class Sub extends Sup { @Override public int v() { return 2; } }

    private static int ownSecret() { return 777; }

    static void testLookup() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        check(lookup.lookupClass() == LookupTest.class, "lookupClass");
        check(lookup.lookupModes() != 0, "lookupModes");

        MethodHandle virtual = lookup.findVirtual(
                String.class, "length", MethodType.methodType(int.class));
        check((int) virtual.invoke("abc") == 3, "findVirtual");

        MethodHandle stat = lookup.findStatic(
                Integer.class, "valueOf", MethodType.methodType(Integer.class, int.class));
        check((Integer) stat.invoke(5) == 5, "findStatic");

        MethodHandle ctor = lookup.findConstructor(
                java.util.ArrayList.class, MethodType.methodType(void.class));
        java.util.ArrayList<?> list = (java.util.ArrayList<?>) ctor.invoke();
        check(list.isEmpty(), "findConstructor");

        MethodHandle getter = lookup.findGetter(
                java.awt.Point.class, "x", int.class);
        check((int) getter.invoke(new java.awt.Point(9, 0)) == 9, "findGetter");

        MethodHandle unreflected = lookup.unreflect(
                String.class.getMethod("length"));
        check((int) unreflected.invoke("xy") == 2, "unreflect");

        check(lookup.in(Integer.class) != null, "in");
    }

    static void testFindVirtualDeep() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        // instance method round-trip on a self-made class
        MethodHandle size = lookup.findVirtual(Sample.class, "size",
                MethodType.methodType(int.class));
        check(size.type().equals(MethodType.methodType(int.class, Sample.class)),
                "findVirtual handle type");
        check((int) size.invokeExact(new Sample()) == 42, "findVirtual precise value");
        // missing member -> NoSuchMethodException
        checkThrows(NoSuchMethodException.class,
                () -> lookup.findVirtual(Sample.class, "nope",
                        MethodType.methodType(int.class)),
                "findVirtual missing NSME");
        // private method: JDK-8 has no nests, so resolution refuses it outright
        // (JDK-11+ resolves nestmate privates successfully)
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findVirtual(Sample.class, "hidden",
                        MethodType.methodType(int.class)),
                "no such method", "findVirtual private IAE (no nests in 8)");
        // public method on public nested class via in()
        MethodHandles.Lookup inSub = lookup.in(Sub.class);
        check((int) inSub.findVirtual(Sub.class, "v",
                MethodType.methodType(int.class)).invoke(new Sub()) == 2,
                "findVirtual through in(Sub)");
    }

    static void testFindStaticDeep() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle s = lookup.findStatic(Sample.class, "stat",
                MethodType.methodType(int.class));
        check((int) s.invokeExact() == 55, "findStatic precise value");
        checkThrows(NoSuchMethodException.class,
                () -> lookup.findStatic(Sample.class, "nope",
                        MethodType.methodType(int.class)),
                "findStatic missing NSME");
        // private static hidden from an outer-class lookup (no nests in 8)
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findStatic(Sample.class, "hiddenStat",
                        MethodType.methodType(int.class)),
                "no such method", "findStatic private IAE (no nests in 8)");
        // wrong method type -> NSME
        checkThrows(NoSuchMethodException.class,
                () -> lookup.findStatic(Sample.class, "stat",
                        MethodType.methodType(long.class)),
                "findStatic wrong type NSME");
    }

    static void testFindGetterSetterDeep() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Sample s = new Sample();
        MethodHandle get = lookup.findGetter(Sample.class, "pub", int.class);
        MethodHandle set = lookup.findSetter(Sample.class, "pub", int.class);
        set.invoke(s, 123);
        // write through the handle is visible to a plain field read
        check(s.pub == 123 && (int) get.invoke(s) == 123, "findSetter then plain read");
        // static getter/setter round-trip
        MethodHandle sget = lookup.findStaticGetter(Sample.class, "stat", int.class);
        MethodHandle sset = lookup.findStaticSetter(Sample.class, "stat", int.class);
        sset.invoke(456);
        check(Sample.stat == 456 && (int) sget.invoke() == 456,
                "findStaticGetter/Setter round-trip");
        // missing field
        checkThrows(NoSuchFieldException.class,
                () -> lookup.findGetter(Sample.class, "nope", int.class),
                "findGetter missing NSFE");
        // wrong field type: reported as NoSuchFieldException with "no such field"
        checkThrowsMsg(NoSuchFieldException.class,
                () -> lookup.findGetter(Sample.class, "pub", long.class),
                "no such field", "findGetter wrong type NSFE message");
        // static field requested through the instance accessor -> IAE
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findGetter(Sample.class, "stat", int.class),
                "expected a non-static field", "findGetter static field IAE");
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findSetter(Sample.class, "stat", int.class),
                "expected a non-static field", "findSetter static field IAE");
        // final field cannot be written via findSetter
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findSetter(Sample.class, "fin", int.class),
                "unexpected set of a final field", "findSetter final IAE");
        // instance field requested through the static accessor -> IAE
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findStaticGetter(Sample.class, "pub", int.class),
                "expected a static field", "findStaticGetter instance field IAE");
    }

    static void testFindSpecialDeep() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        // invokespecial semantics: dispatch the SUPERCLASS implementation
        MethodHandle special = lookup.in(Sub.class).findSpecial(Sup.class, "v",
                MethodType.methodType(int.class), Sub.class);
        check((int) special.invokeExact(new Sub()) == 1, "findSpecial runs super version");
        check(special.type().equals(MethodType.methodType(int.class, Sub.class)),
                "findSpecial handle type takes specialCaller as receiver");
        // specialCaller must be the lookup class (or a subclass relationship)
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findSpecial(Sup.class, "v",
                        MethodType.methodType(int.class), LookupTest.class),
                "caller class must be a subclass below the method",
                "findSpecial wrong specialCaller IAE");
    }

    static void testFindConstructorDeep() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle ctor = lookup.findConstructor(Sample.class,
                MethodType.methodType(void.class));
        Sample s = (Sample) ctor.invokeExact();
        check(s != null && s.pub == 1, "findConstructor round-trip field precise");
        // no matching constructor signature -> NoSuchMethodException
        checkThrows(NoSuchMethodException.class,
                () -> lookup.findConstructor(Sample.class,
                        MethodType.methodType(void.class, String.class)),
                "findConstructor wrong signature NSME");
        // private constructor: unreachable from an outer-class lookup (no nests in 8)
        checkThrows(IllegalAccessException.class,
                () -> lookup.findConstructor(Sample.class,
                        MethodType.methodType(void.class, int.class)),
                "findConstructor private IAE (no nests in 8)");
        // constructor handle type is (void)Sample
        check(ctor.type().equals(MethodType.methodType(Sample.class)),
                "findConstructor handle type");
    }

    static void testUnreflectDeep() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        // method unreflect agrees with reflective invocation
        Method size = Sample.class.getMethod("size");
        MethodHandle um = lookup.unreflect(size);
        check((int) um.invoke(new Sample()) == 42, "unreflect invoke value");
        // private static method: reflective Method object visible, but unreflect
        // denies access from this lookup in JDK-8 (no nests)
        Method hs = Sample.class.getDeclaredMethod("hiddenStat");
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.unreflect(hs), "member is private",
                "unreflect private IAE message");
        // setAccessible(true) opens the door on both releases
        hs.setAccessible(true);
        check((int) lookup.unreflect(hs).invokeExact() == 66,
                "unreflect private after setAccessible");
        hs.setAccessible(false);
        // constructor unreflect
        Constructor<Sample> c = Sample.class.getConstructor();
        Sample s = (Sample) lookup.unreflectConstructor(c).invokeExact();
        check(s.pub == 1, "unreflectConstructor round-trip");
        // getter/setter unreflect with plain-read visibility
        Field pub = Sample.class.getField("pub");
        MethodHandle ug = lookup.unreflectGetter(pub);
        MethodHandle us = lookup.unreflectSetter(pub);
        us.invoke(s, 321);
        check(s.pub == 321 && (int) ug.invoke(s) == 321, "unreflectGetter/Setter");
        // static final field: no write access, exact message
        Field konst = Sample.class.getField("CONST");
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.unreflectSetter(konst),
                "unexpected set of a final field",
                "unreflectSetter static final IAE message (8)");
        // instance final field likewise
        Field fin = Sample.class.getField("fin");
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.unreflectSetter(fin),
                "unexpected set of a final field",
                "unreflectSetter final field IAE");
        // static getter on the final field is fine
        check((int) lookup.unreflectGetter(konst).invoke() == 7,
                "unreflectGetter static final readable");
    }

    static void testInDowngrade() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandles.Lookup inString = lookup.in(String.class);
        check(inString.lookupClass() == String.class, "in changes lookupClass");
        // a JDK-8 in() on another package drops all but PUBLIC
        check(inString.lookupModes() == MethodHandles.Lookup.PUBLIC,
                "in(String) modes reduced to PUBLIC");
        check(inString.toString().contains("java.lang.String"),
                "in(String) toString names class");
        check(inString.toString().contains("public"), "in(String) toString public tag");
        // the downgraded lookup can no longer reach our own private static
        checkThrows(IllegalAccessException.class,
                () -> inString.findStatic(LookupTest.class, "ownSecret",
                        MethodType.methodType(int.class)),
                "downgraded lookup denied own private static");
        // in() on the same class keeps the modes
        MethodHandles.Lookup same = lookup.in(LookupTest.class);
        check(same.lookupModes() == lookup.lookupModes(), "in(same) keeps modes");
        // lookup().toString mentions the lookup class and access context
        check(lookup.toString().contains("LookupTest"), "lookup toString names class");
        // publicLookup has only public access
        check(MethodHandles.publicLookup().lookupClass() == Object.class,
                "publicLookup lookupClass");
        check(MethodHandles.publicLookup().lookupModes() == MethodHandles.Lookup.PUBLIC,
                "publicLookup modes PUBLIC only");
        check(MethodHandles.publicLookup().toString().contains("public"),
                "publicLookup toString public tag");
    }

    static void testVersionSpecificAbsences() throws Exception {
        // JDK-8 Lookup predates these APIs; lock the difference (JDK-9+ adds them)
        checkThrows(NoSuchMethodException.class,
                () -> MethodHandles.class.getMethod("privateLookupIn", Class.class,
                        MethodHandles.Lookup.class),
                "no privateLookupIn in 8");
        checkThrows(NoSuchMethodException.class,
                () -> MethodHandles.Lookup.class.getMethod("dropLookupMode", int.class),
                "no dropLookupMode in 8");
        checkThrows(NoSuchMethodException.class,
                () -> MethodHandles.Lookup.class.getMethod("hasPrivateAccess"),
                "no hasPrivateAccess in 8");
        checkThrows(NoSuchMethodException.class,
                () -> MethodHandles.Lookup.class.getMethod("findClass", String.class),
                "no findClass in 8");
        checkThrows(NoSuchMethodException.class,
                () -> MethodHandles.Lookup.class.getMethod("accessClass", Class.class),
                "no accessClass in 8");
    }

    public static void main(String[] args) throws Throwable {
        testLookup();
        testFindVirtualDeep();
        testFindStaticDeep();
        testFindGetterSetterDeep();
        testFindSpecialDeep();
        testFindConstructorDeep();
        testUnreflectDeep();
        testInDowngrade();
        testVersionSpecificAbsences();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
