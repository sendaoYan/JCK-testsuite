/*
 * @test
 * @summary java.lang.invoke.MethodHandleInfo conformance: white-box + black-box behavior.
 *          Deepened: extended direct-handle kind matrix (findSpecial/findGetter/
 *          findSetter/findStaticGetter/findStaticSetter/interface findVirtual/
 *          unreflectConstructor/unreflectGetter/unreflectSetter) with exact
 *          member/type/modifier assertions, reflectAs round-trip equality for
 *          Method/Field/Constructor plus wrong-kind CCE, info.toString() equals
 *          the static four-argument formatter, revealDirect rejection of
 *          combinator products ("not a direct method handle") and acceptance
 *          of a no-op asType, reflectAs access failures through publicLookup
 *          (package-private and private members).
 * @run main MethodHandleInfoTest
 */

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MethodHandleInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static int sample(int x) { return x; }
    static String format(String f, Object... args) { return String.format(f, args); }

    public interface Iface {
        int len(String s);
    }

    public static class Box {
        public int pub = 1;
        public static int SPUB = 2;
        private int priv = 3;
        int pkg = 4;
        public Box() { }
        public Box(int v) { pub = v; }
    }

    static int add(int a, int b) { return a + b; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.MethodHandleInfo");
        check(c.isInterface(), "is an interface");
        // the nine JVMS reference kinds
        int[] expect = {1, 2, 3, 4, 5, 6, 7, 8, 9};
        String[] fields = {"REF_getField", "REF_getStatic", "REF_putField", "REF_putStatic",
                "REF_invokeVirtual", "REF_invokeStatic", "REF_invokeSpecial",
                "REF_newInvokeSpecial", "REF_invokeInterface"};
        for (int i = 0; i < fields.length; i++) {
            check(c.getField(fields[i]).getInt(null) == expect[i], fields[i] + " == " + expect[i]);
        }
        String[] names = {"getReferenceKind", "getDeclaringClass", "getName", "getMethodType",
                "reflectAs", "getModifiers", "isVarArgs", "referenceKindToString", "toString"};
        for (String n : names) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(n)) found = true;
            check(found, "declares " + n);
        }
        check(c.getDeclaredMethod("isVarArgs").isDefault(), "isVarArgs is default");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // a static method handle round-trips through revealDirect
        MethodHandle mh = lookup.findStatic(MethodHandleInfoTest.class, "sample",
                MethodType.methodType(int.class, int.class));
        MethodHandleInfo info = lookup.revealDirect(mh);
        check(info.getReferenceKind() == MethodHandleInfo.REF_invokeStatic, "static kind");
        check(info.getDeclaringClass() == MethodHandleInfoTest.class, "declaring class");
        check(info.getName().equals("sample"), "name");
        check(info.getMethodType().toMethodDescriptorString().equals("(I)I"), "method type");
        check(java.lang.reflect.Modifier.isStatic(info.getModifiers())
              && !java.lang.reflect.Modifier.isPublic(info.getModifiers()),
              "modifiers reflect the member (static, package-private)");
        check(!info.isVarArgs(), "not varargs");

        // a virtual method handle reports REF_invokeVirtual and its declaring class
        MethodHandle len = lookup.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        MethodHandleInfo lenInfo = lookup.revealDirect(len);
        check(lenInfo.getReferenceKind() == MethodHandleInfo.REF_invokeVirtual, "virtual kind");
        check(lenInfo.getDeclaringClass() == String.class, "String declares length");
        check(lenInfo.getName().equals("length"), "virtual name");

        // varargs detection
        MethodHandle fmt = lookup.findStatic(MethodHandleInfoTest.class, "format",
                MethodType.methodType(String.class, String.class, Object[].class));
        check(lookup.revealDirect(fmt).isVarArgs(), "varargs detected");

        // reflectAs yields an equivalent reflective Member
        Method reflected = info.reflectAs(Method.class, lookup);
        check(reflected.getName().equals("sample"), "reflectAs name");
        check(reflected.invoke(null, 21).equals(21), "reflectAs usable");

        // toString(int, Class, String, MethodType) formats like a member reference
        String s = MethodHandleInfo.toString(MethodHandleInfo.REF_invokeStatic,
                MethodHandleInfoTest.class, "sample",
                MethodType.methodType(int.class, int.class));
        check(s.contains("MethodHandleInfoTest") && s.contains("sample"), "static toString: " + s);

        // referenceKindToString maps each kind to its name
        String[] kindNames = {"getField", "getStatic", "putField", "putStatic",
                "invokeVirtual", "invokeStatic", "invokeSpecial", "newInvokeSpecial",
                "invokeInterface"};
        for (int i = 1; i <= 9; i++) {
            check(MethodHandleInfo.referenceKindToString(i).equals(kindNames[i - 1]),
                  "referenceKindToString(" + i + ")");
        }
        try {
            MethodHandleInfo.referenceKindToString(0);
            check(false, "kind 0 rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "referenceKindToString(0) IAE");
        }
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testKindMatrixExtended();
        testReflectAsRoundTrip();
        testToStringRoundTrip();
        testRevealDirectNegatives();
        testReflectAsAccessFailures();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Part 3: deepened contract (2026-11-05) ----

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
                    passed++; return;
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

    // The handle species not covered by testBlackBox.
    static void testKindMatrixExtended() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandleInfo i;

        i = lookup.revealDirect(lookup.findSpecial(Object.class, "toString",
                MethodType.methodType(String.class), MethodHandleInfoTest.class));
        check(i.getReferenceKind() == MethodHandleInfo.REF_invokeSpecial,
              "findSpecial kind 7");
        check(i.getDeclaringClass() == Object.class, "findSpecial declaring is owner");
        check(i.getMethodType().equals(MethodType.methodType(String.class)),
              "findSpecial type as requested");

        i = lookup.revealDirect(lookup.findGetter(Box.class, "pub", int.class));
        check(i.getReferenceKind() == MethodHandleInfo.REF_getField, "findGetter kind 1");
        check("pub".equals(i.getName()), "findGetter name");
        check(i.getMethodType().equals(MethodType.methodType(int.class)),
              "getter type ()int");
        check(i.getModifiers() == 0x0001, "public field modifier 0x1");

        i = lookup.revealDirect(lookup.findSetter(Box.class, "pub", int.class));
        check(i.getReferenceKind() == MethodHandleInfo.REF_putField, "findSetter kind 3");
        check(i.getMethodType().equals(MethodType.methodType(void.class, int.class)),
              "setter type (int)void");

        i = lookup.revealDirect(lookup.findStaticGetter(Box.class, "SPUB", int.class));
        check(i.getReferenceKind() == MethodHandleInfo.REF_getStatic,
              "findStaticGetter kind 2");
        check(i.getModifiers() == 0x0009, "public static field modifier 0x9");

        i = lookup.revealDirect(lookup.findStaticSetter(Box.class, "SPUB", int.class));
        check(i.getReferenceKind() == MethodHandleInfo.REF_putStatic,
              "findStaticSetter kind 4");

        // interface findVirtual -> REF_invokeInterface, declaring class is the interface
        i = lookup.revealDirect(lookup.findVirtual(Iface.class, "len",
                MethodType.methodType(int.class, String.class)));
        check(i.getReferenceKind() == MethodHandleInfo.REF_invokeInterface,
              "interface findVirtual kind 9");
        check(i.getDeclaringClass() == Iface.class, "interface declares it");
        check((i.getModifiers() & 0x0400) != 0, "interface method has ABSTRACT bit");

        i = lookup.revealDirect(lookup.unreflectConstructor(
                Box.class.getConstructor(int.class)));
        check(i.getReferenceKind() == MethodHandleInfo.REF_newInvokeSpecial,
              "constructor kind 8");
        check("<init>".equals(i.getName()), "constructor name <init>");
        check(i.getMethodType().equals(MethodType.methodType(void.class, int.class)),
              "constructor type (int)void");
        check(i.getDeclaringClass() == Box.class, "constructor declaring");

        Field pubField = Box.class.getField("pub");
        i = lookup.revealDirect(lookup.unreflectGetter(pubField));
        check(i.getReferenceKind() == MethodHandleInfo.REF_getField, "unreflectGetter kind 1");
        i = lookup.revealDirect(lookup.unreflectSetter(pubField));
        check(i.getReferenceKind() == MethodHandleInfo.REF_putField, "unreflectSetter kind 3");

        // varargs modifier detail: 0x0080 (TRANSIENT bit) flags ACC_VARARGS
        MethodHandleInfo var = lookup.revealDirect(lookup.findStatic(
                MethodHandleInfoTest.class, "format",
                MethodType.methodType(String.class, String.class, Object[].class)));
        check((var.getModifiers() & 0x0080) != 0, "varargs 0x80 bit present");
        check(var.getModifiers() == 0x0088, "package-private static varargs == 0x88");
    }

    // reflectAs reproduces the original reflective member (Method.equals semantics).
    static void testReflectAsRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Method addMethod = MethodHandleInfoTest.class.getDeclaredMethod("add",
                int.class, int.class);
        MethodHandleInfo info = lookup.revealDirect(lookup.unreflect(addMethod));
        Method back = info.reflectAs(Method.class, lookup);
        check(back.equals(addMethod), "reflectAs Method equals original");
        check(back.invoke(null, 20, 22).equals(42), "reflected Method invocable");

        Field pubField = Box.class.getField("pub");
        Field backField = lookup.revealDirect(
                lookup.unreflectGetter(pubField)).reflectAs(Field.class, lookup);
        check(backField.equals(pubField), "reflectAs Field equals original");

        Constructor<?> ctor = Box.class.getConstructor(int.class);
        Constructor<?> backCtor = lookup.revealDirect(
                lookup.unreflectConstructor(ctor)).reflectAs(Constructor.class, lookup);
        check(backCtor.equals(ctor), "reflectAs Constructor equals original");
        check(backCtor.newInstance(9) != null, "reflected Constructor usable");

        checkThrowsMsg(ClassCastException.class,
              () -> info.reflectAs(Field.class, lookup),
              "Cannot cast", "reflectAs wrong member kind CCE");
    }

    // info.toString() is exactly the four-argument static formatter.
    static void testToStringRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandleInfo[] infos = {
            lookup.revealDirect(lookup.findStatic(MethodHandleInfoTest.class, "sample",
                    MethodType.methodType(int.class, int.class))),
            lookup.revealDirect(lookup.findVirtual(String.class, "length",
                    MethodType.methodType(int.class))),
            lookup.revealDirect(lookup.findGetter(Box.class, "pub", int.class)),
            lookup.revealDirect(lookup.unreflectConstructor(
                    Box.class.getConstructor(int.class))),
            lookup.revealDirect(lookup.findVirtual(Iface.class, "len",
                    MethodType.methodType(int.class, String.class))),
        };
        for (MethodHandleInfo info : infos) {
            String expect = MethodHandleInfo.toString(info.getReferenceKind(),
                    info.getDeclaringClass(), info.getName(), info.getMethodType());
            check(info.toString().equals(expect), "toString equals formatter: " + info);
        }
        String ctorStr = MethodHandleInfo.toString(MethodHandleInfo.REF_newInvokeSpecial,
                Box.class, "<init>", MethodType.methodType(void.class, int.class));
        check(ctorStr.startsWith("newInvokeSpecial "), "ctor string kind prefix");
        check(ctorStr.contains(".<init>:"), "ctor string member form");
        check(ctorStr.endsWith(":(int)void"), "ctor string descriptor suffix");
    }

    // Composite handles are not revealable; a no-op asType keeps directness.
    static void testRevealDirectNegatives() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle add = lookup.findStatic(MethodHandleInfoTest.class, "add",
                MethodType.methodType(int.class, int.class, int.class));
        MethodHandle virt = lookup.findVirtual(String.class, "length",
                MethodType.methodType(int.class));
        checkThrowsMsg(IllegalArgumentException.class,
              () -> lookup.revealDirect(add.asType(
                      MethodType.methodType(int.class, Integer.class, Integer.class))),
              "not a direct method handle", "revealDirect(asType converted) IAE");
        checkThrowsMsg(IllegalArgumentException.class,
              () -> lookup.revealDirect(MethodHandles.filterArguments(add, 0,
                      MethodHandles.identity(int.class))),
              "not a direct method handle", "revealDirect(filterArguments) IAE");
        checkThrowsMsg(IllegalArgumentException.class,
              () -> lookup.revealDirect(MethodHandles.dropArguments(add, 0, String.class)),
              "not a direct method handle", "revealDirect(dropArguments) IAE");
        checkThrowsMsg(IllegalArgumentException.class,
              () -> lookup.revealDirect(MethodHandles.constant(String.class, "c")),
              "not a direct method handle", "revealDirect(constant) IAE");
        checkThrowsMsg(IllegalArgumentException.class,
              () -> lookup.revealDirect(virt.bindTo("abc")),
              "not a direct method handle", "revealDirect(bindTo) IAE");
        checkThrowsMsg(IllegalArgumentException.class,
              () -> lookup.revealDirect(lookup.findStatic(MethodHandleInfoTest.class,
                      "format", MethodType.methodType(String.class, String.class, Object[].class))
                      .asCollector(Object[].class, 1)),
              "not a direct method handle", "revealDirect(asCollector) IAE");
        MethodHandleInfo noop = lookup.revealDirect(
                add.asType(MethodType.methodType(int.class, int.class, int.class)));
        check(noop.getReferenceKind() == MethodHandleInfo.REF_invokeStatic,
              "no-op asType stays revealable");
        check("add".equals(noop.getName()), "no-op asType keeps the name");
    }

    // reflectAs access checks: publicLookup fails on package-private members
    // and (thanks to nestmates) on private members of the same nest.
    static void testReflectAsAccessFailures() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandles.Lookup pub = MethodHandles.publicLookup();
        MethodHandleInfo pkgPrivate = lookup.revealDirect(
                lookup.findStatic(MethodHandleInfoTest.class, "add",
                        MethodType.methodType(int.class, int.class, int.class)));
        checkThrowsMsg(IllegalArgumentException.class,
              () -> pkgPrivate.reflectAs(Method.class, pub),
              "member is private to package",
              "publicLookup reflectAs package-private method IAE");
        MethodHandleInfo pkgField = lookup.revealDirect(
                lookup.findGetter(Box.class, "pkg", int.class));
        checkThrowsMsg(IllegalArgumentException.class,
              () -> pkgField.reflectAs(Field.class, pub),
              "member is private to package",
              "publicLookup reflectAs package-private field IAE");
        MethodHandleInfo privField = lookup.revealDirect(
                lookup.findGetter(Box.class, "priv", int.class));
        checkThrowsMsg(IllegalArgumentException.class,
              () -> privField.reflectAs(Field.class, pub),
              "member is private", "publicLookup reflectAs private field IAE");
    }
}
