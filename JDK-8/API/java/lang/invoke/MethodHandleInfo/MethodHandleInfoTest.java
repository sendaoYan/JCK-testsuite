/*
 * @test
 * @summary java.lang.invoke.MethodHandleInfo conformance: reveal and reference kinds.
 *          Deepened: exact REF_* constant values (all nine JVMS kinds), the
 *          full direct-handle kind matrix (findStatic/findVirtual/findSpecial/
 *          findGetter/findSetter/findStaticGetter/findStaticSetter/interface
 *          findVirtual/unreflectConstructor/unreflectGetter/unreflectSetter/
 *          unreflect/varargs) with exact declaring class/name/method type/
 *          modifiers/isVarArgs, reflectAs round-trip identity against the
 *          original Method/Field/Constructor, info.toString() equals the
 *          static MethodHandleInfo.toString(...) formatter, referenceKindToString
 *          table plus invalid-kind IAE, revealDirect rejection of composite
 *          handles ("not a direct method handle") and acceptance of a no-op
 *          asType, and reflectAs access failures through publicLookup.
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

    public interface Iface {
        int len(String s);
    }

    public static class Box {
        public int pub = 1;
        public static int SPUB = 2;
        int pkg = 4;
        public Box() { }
        public Box(int v) { pub = v; }
    }

    static int add(int a, int b) { return a + b; }
    public static String greet(String s) { return "hi " + s; }
    public static int sum(int... xs) { int t = 0; for (int x : xs) t += x; return t; }

    static void testMethodHandleInfo() throws Throwable {
        check(MethodHandleInfo.REF_getField == 1, "REF_getField");
        check(MethodHandleInfo.REF_invokeVirtual == 5, "REF_invokeVirtual");
        check(MethodHandleInfo.REF_newInvokeSpecial == 8, "REF_newInvokeSpecial");
        check(MethodHandleInfo.REF_invokeInterface == 9, "REF_invokeInterface");
        check("invokeVirtual".equals(MethodHandleInfo.referenceKindToString(
                MethodHandleInfo.REF_invokeVirtual)), "referenceKindToString");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mh = lookup.findVirtual(
                String.class, "length", MethodType.methodType(int.class));
        MethodHandleInfo info = lookup.revealDirect(mh);
        check(info.getReferenceKind() == MethodHandleInfo.REF_invokeVirtual,
              "getReferenceKind");
        check(info.getDeclaringClass() == String.class, "getDeclaringClass");
        check("length".equals(info.getName()), "getName");
        check(info.getMethodType().equals(MethodType.methodType(int.class)), "getMethodType");
        check(info.getModifiers() != 0, "getModifiers");
        check(!info.isVarArgs(), "isVarArgs");
    }

    // All nine JVMS 5.4.3.5 reference kinds with their exact constant values.
    static void testReferenceKindConstants() {
        check(MethodHandleInfo.REF_getField == 1, "REF_getField == 1");
        check(MethodHandleInfo.REF_getStatic == 2, "REF_getStatic == 2");
        check(MethodHandleInfo.REF_putField == 3, "REF_putField == 3");
        check(MethodHandleInfo.REF_putStatic == 4, "REF_putStatic == 4");
        check(MethodHandleInfo.REF_invokeVirtual == 5, "REF_invokeVirtual == 5");
        check(MethodHandleInfo.REF_invokeStatic == 6, "REF_invokeStatic == 6");
        check(MethodHandleInfo.REF_invokeSpecial == 7, "REF_invokeSpecial == 7");
        check(MethodHandleInfo.REF_newInvokeSpecial == 8, "REF_newInvokeSpecial == 8");
        check(MethodHandleInfo.REF_invokeInterface == 9, "REF_invokeInterface == 9");
    }

    // The full factory matrix: every direct-handle species reveals the right
    // kind, member and signature.
    static void testKindMatrix() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandleInfo i;

        i = lookup.revealDirect(lookup.findStatic(MethodHandleInfoTest.class, "add",
                MethodType.methodType(int.class, int.class, int.class)));
        check(i.getReferenceKind() == MethodHandleInfo.REF_invokeStatic, "findStatic kind 6");
        check(i.getDeclaringClass() == MethodHandleInfoTest.class, "findStatic declaring");
        check("add".equals(i.getName()), "findStatic name");
        check(i.getMethodType().equals(
                MethodType.methodType(int.class, int.class, int.class)), "findStatic type");
        check(i.getModifiers() == 0x0008, "package-private static modifier 0x8");
        check(!i.isVarArgs(), "findStatic not varargs");

        i = lookup.revealDirect(lookup.findVirtual(String.class, "length",
                MethodType.methodType(int.class)));
        check(i.getReferenceKind() == MethodHandleInfo.REF_invokeVirtual, "findVirtual kind 5");
        check(i.getDeclaringClass() == String.class, "findVirtual declaring");
        check("length".equals(i.getName()), "findVirtual name");

        i = lookup.revealDirect(lookup.findSpecial(Object.class, "toString",
                MethodType.methodType(String.class), MethodHandleInfoTest.class));
        check(i.getReferenceKind() == MethodHandleInfo.REF_invokeSpecial, "findSpecial kind 7");
        check(i.getDeclaringClass() == Object.class, "findSpecial declaring is owner");
        check("toString".equals(i.getName()), "findSpecial name");
        check(i.getMethodType().equals(MethodType.methodType(String.class)),
              "findSpecial type as requested");

        i = lookup.revealDirect(lookup.findGetter(Box.class, "pub", int.class));
        check(i.getReferenceKind() == MethodHandleInfo.REF_getField, "findGetter kind 1");
        check(i.getDeclaringClass() == Box.class, "findGetter declaring");
        check("pub".equals(i.getName()), "findGetter name");
        check(i.getMethodType().equals(MethodType.methodType(int.class)),
              "getter type is ()int");
        check(i.getModifiers() == 0x0001, "public field modifier 0x1");

        i = lookup.revealDirect(lookup.findSetter(Box.class, "pub", int.class));
        check(i.getReferenceKind() == MethodHandleInfo.REF_putField, "findSetter kind 3");
        check(i.getMethodType().equals(MethodType.methodType(void.class, int.class)),
              "setter type is (int)void");

        i = lookup.revealDirect(lookup.findStaticGetter(Box.class, "SPUB", int.class));
        check(i.getReferenceKind() == MethodHandleInfo.REF_getStatic, "findStaticGetter kind 2");
        check(i.getModifiers() == 0x0009, "public static field modifier 0x9");

        i = lookup.revealDirect(lookup.findStaticSetter(Box.class, "SPUB", int.class));
        check(i.getReferenceKind() == MethodHandleInfo.REF_putStatic, "findStaticSetter kind 4");
        check(i.getMethodType().equals(MethodType.methodType(void.class, int.class)),
              "static setter type (int)void");

        // findVirtual on an interface method reveals REF_invokeInterface and
        // the interface itself as the declaring class.
        i = lookup.revealDirect(lookup.findVirtual(Iface.class, "len",
                MethodType.methodType(int.class, String.class)));
        check(i.getReferenceKind() == MethodHandleInfo.REF_invokeInterface,
              "interface findVirtual kind 9");
        check(i.getDeclaringClass() == Iface.class, "interface declaring is the interface");
        check("len".equals(i.getName()), "interface method name");
        check((i.getModifiers() & 0x0400) != 0, "interface method modifier has ABSTRACT");

        i = lookup.revealDirect(lookup.unreflectConstructor(
                Box.class.getConstructor(int.class)));
        check(i.getReferenceKind() == MethodHandleInfo.REF_newInvokeSpecial,
              "constructor kind 8");
        check("<init>".equals(i.getName()), "constructor name is <init>");
        check(i.getMethodType().equals(MethodType.methodType(void.class, int.class)),
              "constructor type (int)void");
        check(i.getDeclaringClass() == Box.class, "constructor declaring");

        Field pubField = Box.class.getField("pub");
        i = lookup.revealDirect(lookup.unreflectGetter(pubField));
        check(i.getReferenceKind() == MethodHandleInfo.REF_getField, "unreflectGetter kind 1");
        check("pub".equals(i.getName()), "unreflectGetter name");

        i = lookup.revealDirect(lookup.unreflectSetter(pubField));
        check(i.getReferenceKind() == MethodHandleInfo.REF_putField, "unreflectSetter kind 3");

        Method addMethod = MethodHandleInfoTest.class.getDeclaredMethod("add",
                int.class, int.class);
        i = lookup.revealDirect(lookup.unreflect(addMethod));
        check(i.getReferenceKind() == MethodHandleInfo.REF_invokeStatic, "unreflect kind 6");
        check("add".equals(i.getName()), "unreflect name");

        i = lookup.revealDirect(lookup.findStatic(MethodHandleInfoTest.class, "sum",
                MethodType.methodType(int.class, int[].class)));
        check(i.isVarArgs(), "varargs method detected");
        check((i.getModifiers() & 0x0080) != 0, "varargs flag bit 0x80 present");
        check((i.getModifiers() & 0x0008) != 0, "static bit present");
    }

    // reflectAs must reproduce the original reflective member object.
    static void testReflectAsRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Method addMethod = MethodHandleInfoTest.class.getDeclaredMethod("add",
                int.class, int.class);
        MethodHandleInfo info = lookup.revealDirect(lookup.unreflect(addMethod));
        Method back = info.reflectAs(Method.class, lookup);
        check(back.equals(addMethod), "reflectAs Method equals original");
        check(back.invoke(null, 20, 22).equals(42), "reflected Method still invocable");

        Field pubField = Box.class.getField("pub");
        Field backField = lookup.revealDirect(
                lookup.unreflectGetter(pubField)).reflectAs(Field.class, lookup);
        check(backField.equals(pubField), "reflectAs Field equals original");

        Constructor<?> ctor = Box.class.getConstructor(int.class);
        Constructor<?> backCtor = lookup.revealDirect(
                lookup.unreflectConstructor(ctor)).reflectAs(Constructor.class, lookup);
        check(backCtor.equals(ctor), "reflectAs Constructor equals original");
        check(backCtor.newInstance(5) != null, "reflected Constructor still usable");

        // reflecting a method as a Field is a type confusion rejected with CCE
        checkThrowsMsg(ClassCastException.class,
              () -> info.reflectAs(Field.class, lookup),
              "Cannot cast", "reflectAs wrong member kind CCE");
    }

    // info.toString() and the four-argument static formatter agree.
    static void testToStringRoundTrip() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandleInfo[] infos = {
            lookup.revealDirect(lookup.findStatic(MethodHandleInfoTest.class, "add",
                    MethodType.methodType(int.class, int.class, int.class))),
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
            check(info.toString().equals(expect),
                  "toString equals formatter: " + info);
        }
        String ctorStr = MethodHandleInfo.toString(MethodHandleInfo.REF_newInvokeSpecial,
                Box.class, "<init>", MethodType.methodType(void.class, int.class));
        check(ctorStr.startsWith("newInvokeSpecial "), "ctor string kind prefix");
        check(ctorStr.contains(".<init>:"), "ctor string member form");
        check(ctorStr.endsWith(":(int)void"), "ctor string descriptor suffix");
    }

    // referenceKindToString maps 1..9 exactly; anything else is IAE.
    static void testReferenceKindToStringTable() {
        String[] names = {"getField", "getStatic", "putField", "putStatic",
                "invokeVirtual", "invokeStatic", "invokeSpecial", "newInvokeSpecial",
                "invokeInterface"};
        for (int kind = 1; kind <= 9; kind++) {
            check(MethodHandleInfo.referenceKindToString(kind).equals(names[kind - 1]),
                  "referenceKindToString(" + kind + ") == " + names[kind - 1]);
        }
        checkThrowsMsg(IllegalArgumentException.class,
              () -> MethodHandleInfo.referenceKindToString(0),
              "invalid reference kind", "kind 0 IAE message");
        checkThrowsMsg(IllegalArgumentException.class,
              () -> MethodHandleInfo.referenceKindToString(10),
              "invalid reference kind", "kind 10 IAE message");
    }

    // revealDirect only accepts direct handles: every combinator product is
    // rejected with IAE "not a direct method handle".
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
                      "sum", MethodType.methodType(int.class, int[].class))
                      .asCollector(int[].class, 2)),
              "not a direct method handle", "revealDirect(asCollector) IAE");
        // asType with an identical type is a no-op: the handle stays direct.
        MethodHandleInfo noop = lookup.revealDirect(
                add.asType(MethodType.methodType(int.class, int.class, int.class)));
        check(noop.getReferenceKind() == MethodHandleInfo.REF_invokeStatic,
              "no-op asType stays revealable");
        check("add".equals(noop.getName()), "no-op asType keeps the name");
    }

    // reflectAs performs an access check: publicLookup cannot reflect members
    // it could not have produced handles for; both a package-private method
    // and a package-private field fail with IAE wrapping the underlying
    // IllegalAccessException ("member is private to package").
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
    }

    public static void main(String[] args) throws Throwable {
        testMethodHandleInfo();
        testReferenceKindConstants();
        testKindMatrix();
        testReflectAsRoundTrip();
        testToStringRoundTrip();
        testReferenceKindToStringTable();
        testRevealDirectNegatives();
        testReflectAsAccessFailures();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
