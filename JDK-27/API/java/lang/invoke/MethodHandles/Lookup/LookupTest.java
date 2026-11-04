/*
 * @test
 * @summary java.lang.invoke.MethodHandles.Lookup conformance: white-box + black-box behavior,
 *          deepened with exact access-denial messages ("no such field", "expected a
 *          non-static field", "unexpected set of a final field", "static final field
 *          has no write access", "member is private"), nestmate private access
 *          (findVirtual/findGetter/findConstructor on private members), privateLookupIn
 *          semantics (null NPE, cross-module IAE "module java.base", full privilege
 *          transfer, previousLookupClass), dropLookupMode exact mode arithmetic
 *          (17/25/15), and ensureInitialized/accessClass/findClass contracts.
 * @run main LookupTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class LookupTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static class Holder {
        public int x = 7;
        public static int STAT = 100;
        public String name;
        public Holder() { name = "ctor"; }
        public int size() { return 42; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MethodHandles$Lookup");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        for (int k : new int[]{1, 2, 4, 8, 16, 32, 64}) {
            check(c.getField(modeName(k)).getType() == int.class, "mode constant " + k);
        }
        String[] names = {"lookupClass", "lookupModes", "in", "dropLookupMode",
                "findStatic", "findVirtual", "findConstructor", "findSpecial", "findGetter",
                "findSetter", "findVarHandle", "findStaticGetter", "findStaticSetter",
                "findClass", "accessClass", "ensureInitialized", "defineClass",
                "defineHiddenClass", "bind", "unreflect", "unreflectGetter", "unreflectSetter",
                "unreflectConstructor", "unreflectVarHandle", "revealDirect", "toString",
                "hasPrivateAccess", "hasFullPrivilegeAccess"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
    }

    static String modeName(int v) {
        switch (v) {
            case 1: return "PUBLIC";
            case 2: return "PRIVATE";
            case 4: return "PROTECTED";
            case 8: return "PACKAGE";
            case 16: return "MODULE";
            case 32: return "UNCONDITIONAL";
            default: return "ORIGINAL";
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        Lookup lookup = MethodHandles.lookup();

        // identity of the lookup and its access modes
        check(lookup.lookupClass() == LookupTest.class, "lookupClass");
        check(lookup.lookupModes() == (Lookup.PUBLIC | Lookup.PRIVATE | Lookup.PROTECTED
                | Lookup.PACKAGE | Lookup.MODULE | Lookup.ORIGINAL),
              "lookup() has full modes: " + lookup.lookupModes());
        check(lookup.hasPrivateAccess(), "hasPrivateAccess");
        check(lookup.hasFullPrivilegeAccess(), "hasFullPrivilegeAccess");
        check(MethodHandles.publicLookup().lookupModes() == Lookup.UNCONDITIONAL,
              "publicLookup has only UNCONDITIONAL");

        // toString mentions the lookup class
        check(lookup.toString().contains("LookupTest"), "toString names class");

        // findVirtual / findStatic / findConstructor / access
        MethodHandle size = lookup.findVirtual(Holder.class, "size",
                MethodType.methodType(int.class));
        check((int) size.invoke(new Holder()) == 42, "findVirtual");
        MethodHandle parseInt = lookup.findStatic(Integer.class, "parseInt",
                MethodType.methodType(int.class, String.class));
        check((int) parseInt.invokeExact("77") == 77, "findStatic");
        MethodHandle ctor = lookup.findConstructor(Holder.class,
                MethodType.methodType(void.class));
        check(((Holder) ctor.invokeExact()).name.equals("ctor"), "findConstructor");
        check(lookup.findClass("java.lang.String") == String.class, "findClass");
        check(lookup.accessClass(Holder.class) == Holder.class, "accessClass");

        // field access
        MethodHandle getX = lookup.findGetter(Holder.class, "x", int.class);
        Holder h = new Holder();
        check((int) getX.invoke(h) == 7, "findGetter");
        MethodHandle setX = lookup.findSetter(Holder.class, "x", int.class);
        setX.invoke(h, 11);
        check(h.x == 11, "findSetter");
        MethodHandle stat = lookup.findStaticGetter(Holder.class, "STAT", int.class);
        check((int) stat.invoke() == 100, "findStaticGetter");
        check((int) lookup.findVirtual(String.class, "length",
                MethodType.methodType(int.class)).invokeExact((String) "abcd") == 4,
              "findVirtual on String");

        // findVarHandle gives a working VarHandle
        java.lang.invoke.VarHandle vh = lookup.findVarHandle(Holder.class, "x", int.class);
        check((int) vh.get(h) == 11, "findVarHandle get");
        vh.set(h, 21);
        check(h.x == 21, "findVarHandle set");

        // unreflect a reflective Method
        Method nameLen = String.class.getMethod("length");
        MethodHandle un = lookup.unreflect(nameLen);
        check((int) un.invokeExact((String) "xyz") == 3, "unreflect");

        // bind attaches a virtual method to a receiver
        MethodHandle bound = lookup.bind("abcde", "length",
                MethodType.methodType(int.class));
        check((int) bound.invokeExact() == 5, "bind");

        // revealDirect round trips to MethodHandleInfo
        MethodHandleInfo info = lookup.revealDirect(parseInt);
        check(info.getReferenceKind() == MethodHandleInfo.REF_invokeStatic, "revealDirect kind");
        check(info.getDeclaringClass() == Integer.class, "revealDirect declaring class");
        check(info.getName().equals("parseInt"), "revealDirect name");

        // in() restricts modes to the intersection
        Lookup inString = lookup.in(String.class);
        check(inString.lookupClass() == String.class, "in changes lookupClass");
        check(inString.lookupModes() == Lookup.PUBLIC, "in(String) keeps PUBLIC only");
        // and loses private access to our own members
        try {
            inString.findStatic(LookupTest.class, "secret",
                    MethodType.methodType(int.class));
            check(false, "restricted lookup must not see our private static");
        } catch (IllegalAccessException e) {
            check(true, "restricted lookup denied private member");
        }
        check(lookup.in(LookupTest.class).lookupModes() == lookup.lookupModes(),
              "in(same class) keeps modes");

        // dropLookupMode removes modes
        check(lookup.dropLookupMode(Lookup.PRIVATE).lookupModes()
                      == ((lookup.lookupModes() & ~(Lookup.PRIVATE | Lookup.PROTECTED
                              | Lookup.ORIGINAL))),
              "dropLookupMode(PRIVATE) drops PROTECTED and ORIGINAL too");
        check(lookup.dropLookupMode(Lookup.PROTECTED).lookupModes()
                      == ((lookup.lookupModes() & ~(Lookup.PROTECTED | Lookup.ORIGINAL))),
              "dropLookupMode(PROTECTED)");

        // privateLookupIn grants access into a public class in our own package/module
        Lookup intoHolder = MethodHandles.privateLookupIn(Holder.class, lookup);
        check(intoHolder.lookupClass() == Holder.class, "privateLookupIn target class");
        check(intoHolder.hasPrivateAccess(), "privateLookupIn grants private access");

        // failed lookups report precise exceptions
        try {
            lookup.findVirtual(String.class, "noSuchMethod", MethodType.methodType(int.class));
            check(false, "findVirtual missing member must throw");
        } catch (NoSuchMethodException e) {
            check(true, "NoSuchMethodException");
        }
        try {
            lookup.findGetter(String.class, "value", int.class);
            check(false, "findGetter wrong type must throw");
        } catch (NoSuchFieldException e) {
            check(true, "NoSuchFieldException");
        }
    }

    private static int secret() { return 1; }

    // ---- Part 3: deepened contracts (access messages, nests, privateLookupIn) ----
    public static class NestTarget {
        private int privField = 21;
        private NestTarget(int x) { privField = x; }
        private int privMethod() { return 87; }
        public static int pubStat = 4;
        public static final int PUB_CONST = 8;
        public final int pubFinal = 6;
        public NestTarget() { }
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

    static void testAccessMessages() throws Throwable {
        Lookup lookup = MethodHandles.lookup();
        // wrong field type is reported as NoSuchFieldException naming the sought signature
        checkThrowsMsg(NoSuchFieldException.class,
                () -> lookup.findGetter(NestTarget.class, "pubStat", long.class),
                "no such field", "findGetter wrong type NSFE message");
        // static field through the instance accessor -> IAE
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findGetter(NestTarget.class, "pubStat", int.class),
                "expected a non-static field", "findGetter static field IAE message");
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findSetter(NestTarget.class, "pubStat", int.class),
                "expected a non-static field", "findSetter static field IAE message");
        // instance field through the static accessor -> IAE
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findStaticGetter(NestTarget.class, "pubFinal", int.class),
                "expected a static field", "findStaticGetter instance field IAE message");
        // final field setters are refused
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.findSetter(NestTarget.class, "pubFinal", int.class),
                "unexpected set of a final field", "findSetter final IAE message");
    }

    static void testUnreflectAccessMessages() throws Throwable {
        Lookup lookup = MethodHandles.lookup();
        java.lang.reflect.Method pm = NestTarget.class.getDeclaredMethod("privMethod");
        // nestmates: our own lookup can unreflect a private member directly
        check((int) lookup.unreflect(pm).invoke(new NestTarget()) == 87,
                "unreflect private nestmate OK");
        // publicLookup cannot
        checkThrowsMsg(IllegalAccessException.class,
                () -> MethodHandles.publicLookup().unreflect(pm),
                "member is private", "publicLookup unreflect private IAE message");
        // downgraded lookup cannot either
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.in(String.class).unreflect(pm),
                "member is private", "in(String) unreflect private IAE message");
        // static final field has no write access through unreflectSetter
        java.lang.reflect.Field f = NestTarget.class.getField("PUB_CONST");
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.unreflectSetter(f),
                "static final field has no write access",
                "unreflectSetter static final IAE message (JDK-9+ wording)");
        // instance final field
        java.lang.reflect.Field fin = NestTarget.class.getField("pubFinal");
        checkThrowsMsg(IllegalAccessException.class,
                () -> lookup.unreflectSetter(fin),
                "unexpected set of a final field", "unreflectSetter final IAE message");
        // reading the static final through a getter is fine
        check((int) lookup.unreflectGetter(f).invoke() == 8,
                "unreflectGetter static final readable");
    }

    static void testNestPrivateAccess() throws Throwable {
        Lookup lookup = MethodHandles.lookup();
        // nests (JDK-11+): private members of our own nested classes resolve and run
        MethodHandle pm = lookup.findVirtual(NestTarget.class, "privMethod",
                MethodType.methodType(int.class));
        check((int) pm.invokeExact(new NestTarget()) == 87,
                "findVirtual private nestmate (JDK-11+ nests)");
        MethodHandle pf = lookup.findGetter(NestTarget.class, "privField", int.class);
        check((int) pf.invoke(new NestTarget()) == 21, "findGetter private nestmate");
        // private constructor is reachable too, and initializes its field
        MethodHandle pc = lookup.findConstructor(NestTarget.class,
                MethodType.methodType(void.class, int.class));
        check((int) lookup.findGetter(NestTarget.class, "privField", int.class)
                .invoke((NestTarget) pc.invoke(77)) == 77,
                "findConstructor private nestmate round-trip");
    }

    static void testPrivateLookupInDeep() throws Throwable {
        Lookup lookup = MethodHandles.lookup();
        // null target class -> NPE
        try {
            MethodHandles.privateLookupIn(null, lookup);
            throw new AssertionError("expected NullPointerException");
        } catch (NullPointerException e) {
            passed++;
        }
        // a JDK-internal class is not open to our unnamed module -> IAE
        checkThrowsMsg(IllegalAccessException.class,
                () -> MethodHandles.privateLookupIn(String.class, lookup),
                "module java.base", "privateLookupIn cross-module IAE message");
        // own nested public class: full privilege transfer
        Lookup into = MethodHandles.privateLookupIn(Holder.class, lookup);
        check(into.lookupClass() == Holder.class, "privateLookupIn lookupClass");
        check(into.hasPrivateAccess() && into.hasFullPrivilegeAccess(),
                "privateLookupIn full privileges");
        check(into.previousLookupClass() == null,
                "same-module privateLookupIn keeps previousLookupClass null");
        check(into.toString().contains("Holder"), "privateLookupIn toString names target");
        // the transferred lookup can now read a private static field of the previous class
        check((int) into.findStaticGetter(LookupTest.class, "hiddenMark", int.class)
                .invoke() == 31337,
                "privateLookupIn reads private of previous class");
        // null lookup -> NPE
        try {
            MethodHandles.privateLookupIn(Holder.class, null);
            throw new AssertionError("expected NullPointerException");
        } catch (NullPointerException e) {
            passed++;
        }
    }

    static void testDropLookupModeExact() throws IllegalAccessException {
        Lookup lookup = MethodHandles.lookup();
        int full = lookup.lookupModes(); // PUBLIC|PRIVATE|PROTECTED|PACKAGE|MODULE|ORIGINAL == 95
        check(full == (Lookup.PUBLIC | Lookup.PRIVATE | Lookup.PROTECTED
                | Lookup.PACKAGE | Lookup.MODULE | Lookup.ORIGINAL),
                "full lookup modes == 95: " + full);
        // dropping PACKAGE also drops PRIVATE/PROTECTED/ORIGINAL -> PUBLIC|MODULE == 17
        check(lookup.dropLookupMode(Lookup.PACKAGE).lookupModes()
                == (Lookup.PUBLIC | Lookup.MODULE), "dropLookupMode(PACKAGE) == 17");
        // dropping PRIVATE also drops PROTECTED/ORIGINAL -> PUBLIC|PACKAGE|MODULE == 25
        check(lookup.dropLookupMode(Lookup.PRIVATE).lookupModes()
                == (Lookup.PUBLIC | Lookup.PACKAGE | Lookup.MODULE),
                "dropLookupMode(PRIVATE) == 25");
        // dropping MODULE collapses to PUBLIC only (module access underpins the rest)
        check(lookup.dropLookupMode(Lookup.MODULE).lookupModes() == Lookup.PUBLIC,
                "dropLookupMode(MODULE) == PUBLIC only");
        // dropping PUBLIC removes every mode
        check(lookup.dropLookupMode(Lookup.PUBLIC).lookupModes() == 0,
                "dropLookupMode(PUBLIC) == 0");
        // previousLookupClass: null on a fresh lookup, original class after in()
        check(lookup.previousLookupClass() == null, "fresh lookup previousLookupClass null");
        check(lookup.in(String.class).previousLookupClass() == LookupTest.class,
                "in() records previous lookup class");
        // same-module privateLookupIn leaves previousLookupClass null (no module hop)
        check(MethodHandles.privateLookupIn(Holder.class, lookup).previousLookupClass()
                == null, "same-module privateLookupIn previousLookupClass null");
    }

    static void testEnsureInitializedDeep() throws Throwable {
        Lookup lookup = MethodHandles.lookup();
        check(lookup.ensureInitialized(String.class) == String.class,
                "ensureInitialized returns the class");
        check(lookup.ensureInitialized(LookupTest.class) == LookupTest.class,
                "ensureInitialized own class");
        check(lookup.accessClass(String.class) == String.class, "accessClass public class");
        check(lookup.findClass("java.lang.invoke.MethodHandles")
                == MethodHandles.class, "findClass resolves FQCN");
        checkThrows(ClassNotFoundException.class,
                () -> lookup.findClass("no.such.Cls"), "findClass missing CNFE");
    }

    private static int hiddenMark = 31337;

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testAccessMessages();
        testUnreflectAccessMessages();
        testNestPrivateAccess();
        testPrivateLookupInDeep();
        testDropLookupModeExact();
        testEnsureInitializedDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
