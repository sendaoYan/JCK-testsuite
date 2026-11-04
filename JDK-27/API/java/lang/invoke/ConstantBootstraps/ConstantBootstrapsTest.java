/*
 * @test
 * @summary java.lang.invoke.ConstantBootstraps conformance: white-box + black-box behavior, full method surface, primitive descriptors, null semantics and precise failure modes.
 * @run main ConstantBootstrapsTest
 */

import java.lang.invoke.ConstantBootstraps;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ConstantBootstrapsTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // fields used by getStaticFinal / staticFieldVarHandle
    static final String VERSION = "v27";
    static int counter = 40;

    public static class Point {
        public int x = 7;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.ConstantBootstraps");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        int pubCtors = 0;
        for (Constructor<?> ct : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(ct.getModifiers())) pubCtors++;
        }
        check(pubCtors == 0, "no public constructor");
        for (String m : new String[]{"enumConstant", "getStaticFinal", "nullConstant",
                                      "fieldVarHandle", "staticFieldVarHandle", "arrayVarHandle",
                                      "explicitCast"}) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        for (Method mm : c.getDeclaredMethods()) {
            if (Modifier.isPublic(mm.getModifiers())) {
                check(Modifier.isStatic(mm.getModifiers()), mm.getName() + " is static");
                check(Modifier.isPublic(mm.getModifiers()), mm.getName() + " is public");
            }
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // enumConstant: name is looked up in the enum class
        Object rt = ConstantBootstraps.enumConstant(lookup, "SOURCE",
                java.lang.annotation.RetentionPolicy.class);
        check(rt == java.lang.annotation.RetentionPolicy.SOURCE, "enumConstant resolves SOURCE");
        check(ConstantBootstraps.enumConstant(lookup, "RUNTIME",
                java.lang.annotation.RetentionPolicy.class)
                == java.lang.annotation.RetentionPolicy.RUNTIME, "enumConstant RUNTIME");
        try {
            ConstantBootstraps.enumConstant(lookup, "NOPE",
                    java.lang.annotation.RetentionPolicy.class);
            check(false, "unknown enum constant rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "unknown enum constant -> IllegalArgumentException");
        }

        // nullConstant: type parameter is only recorded, value is always null
        check(ConstantBootstraps.nullConstant(lookup, "n", String.class) == null,
              "nullConstant returns null");
        try {
            ConstantBootstraps.nullConstant(lookup, "n", int.class);
            check(false, "primitive type rejected");
        } catch (IllegalArgumentException expected) {
            check(true, "nullConstant primitive type -> IllegalArgumentException");
        }

        // getStaticFinal (4-arg): final field of the named declaring class
        Object max = ConstantBootstraps.getStaticFinal(lookup, "MAX_VALUE",
                int.class, Integer.class);
        check(max.equals(2147483647), "getStaticFinal Integer.MAX_VALUE");
        Object v = ConstantBootstraps.getStaticFinal(lookup, "VERSION", String.class,
                ConstantBootstrapsTest.class);
        check("v27".equals(v), "getStaticFinal field of own class");
        // getStaticFinal (3-arg): declaring class derived from the field type,
        // primitives map to their wrapper class
        Object max3 = ConstantBootstraps.getStaticFinal(lookup, "MAX_VALUE", int.class);
        check(max3.equals(2147483647), "3-arg getStaticFinal derives Integer wrapper");
        Object self = ConstantBootstraps.getStaticFinal(lookup, "SOURCE",
                java.lang.annotation.RetentionPolicy.class);
        check(self == java.lang.annotation.RetentionPolicy.SOURCE,
              "3-arg getStaticFinal self-typed constant");
        try {
            ConstantBootstraps.getStaticFinal(lookup, "NO_SUCH", String.class,
                    ConstantBootstrapsTest.class);
            check(false, "absent field rejected");
        } catch (NoSuchFieldError expected) {
            check(true, "absent field -> NoSuchFieldError");
        }

        // fieldVarHandle: instance field access
        VarHandle px = ConstantBootstraps.fieldVarHandle(lookup, "x",
                VarHandle.class, Point.class, int.class);
        Point p = new Point();
        check((int) px.get(p) == 7, "fieldVarHandle initial value");
        px.set(p, 12);
        check(p.x == 12, "fieldVarHandle writes through");
        check(px.varType() == int.class, "fieldVarHandle varType");

        // staticFieldVarHandle
        VarHandle ctr = ConstantBootstraps.staticFieldVarHandle(lookup, "counter",
                VarHandle.class, ConstantBootstrapsTest.class, int.class);
        check((int) ctr.get() == 40, "staticFieldVarHandle initial value");
        ctr.set(41);
        check(counter == 41, "staticFieldVarHandle writes through");

        // arrayVarHandle
        VarHandle avh = ConstantBootstraps.arrayVarHandle(lookup, "a",
                VarHandle.class, int[].class);
        int[] arr = {5, 6, 7};
        check((int) avh.get(arr, 1) == 6, "arrayVarHandle read");
        avh.set(arr, 1, 60);
        check(arr[1] == 60, "arrayVarHandle write");
        check((int) avh.get(arr, 2) == 7, "other elements untouched");

        // explicitCast: identity when types line up, box-compatible, CCE otherwise
        check(ConstantBootstraps.explicitCast(lookup, "c", Integer.class, 42).equals(42),
              "explicitCast identity");
        check(ConstantBootstraps.explicitCast(lookup, "c", Number.class, 42).equals(42),
              "explicitCast to supertype");
        check(ConstantBootstraps.explicitCast(lookup, "c", Integer.class, null) == null,
              "explicitCast keeps null");
        try {
            ConstantBootstraps.explicitCast(lookup, "c", Integer.class, "str");
            check(false, "incompatible cast rejected");
        } catch (ClassCastException expected) {
            check(true, "explicitCast mismatch -> ClassCastException");
        }
    }

    // ---- helpers, self-authored enum and fields ----

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

    enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }

    static final int MAGIC = 4242;
    static String mutableStatic = "m"; // deliberately NOT final

    static String decorate(String s, int n) { return s + "_" + n; }

    // ---- exact public method surface ----
    static void testApiSurface() throws Exception {
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (Method m : ConstantBootstraps.class.getMethods()) {
            if (m.getDeclaringClass() == ConstantBootstraps.class) names.add(m.getName());
        }
        check(names.equals(new java.util.TreeSet<String>(java.util.Arrays.asList(
                "nullConstant", "primitiveClass", "enumConstant", "getStaticFinal",
                "invoke", "fieldVarHandle", "staticFieldVarHandle", "arrayVarHandle",
                "explicitCast"))), "public method surface is closed: " + names);
        for (Method m : ConstantBootstraps.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && Modifier.isStatic(m.getModifiers())) {
                Class<?>[] ps = m.getParameterTypes();
                check(ps.length >= 3 && ps[0] == MethodHandles.Lookup.class
                        && ps[1] == String.class && ps[2] == Class.class,
                        "BSM shape of " + m.getName());
            }
        }
    }

    // ---- enumConstant: exhaustive positives, precise negatives ----
    static void testEnumConstantDeep() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        check(ConstantBootstraps.enumConstant(lookup, "CLUBS", Suit.class) == Suit.CLUBS,
                "enumConstant CLUBS");
        check(ConstantBootstraps.enumConstant(lookup, "DIAMONDS", Suit.class) == Suit.DIAMONDS,
                "enumConstant DIAMONDS");
        check(ConstantBootstraps.enumConstant(lookup, "HEARTS", Suit.class) == Suit.HEARTS,
                "enumConstant HEARTS");
        check(ConstantBootstraps.enumConstant(lookup, "SPADES", Suit.class) == Suit.SPADES,
                "enumConstant SPADES");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> ConstantBootstraps.enumConstant(lookup, "BLUE", Suit.class),
                "No enum constant", "unknown constant rejected");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> ConstantBootstraps.enumConstant(lookup, "clubs", Suit.class),
                "No enum constant", "case-sensitive name");
        checkThrows(NullPointerException.class,
                () -> ConstantBootstraps.enumConstant(lookup, null, Suit.class),
                "null name -> NPE");
        checkThrows(NullPointerException.class,
                () -> ConstantBootstraps.enumConstant(lookup, "CLUBS", null),
                "null enum class -> NPE");
        checkThrows(NullPointerException.class,
                () -> ConstantBootstraps.enumConstant(null, "CLUBS", Suit.class),
                "null lookup -> NPE");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> ConstantBootstraps.enumConstant(lookup, "x", (Class) String.class),
                "is not an enum class", "non-enum type rejected");
    }

    // ---- nullConstant and primitiveClass: documented type rules ----
    static void testConstantTypes() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        check(ConstantBootstraps.nullConstant(lookup, "n", Object.class) == null,
                "nullConstant Object");
        check(ConstantBootstraps.nullConstant(lookup, "n", String[].class) == null,
                "nullConstant array type");
        // lookup and name are declared unused: null is accepted
        check(ConstantBootstraps.nullConstant(null, "n", java.util.List.class) == null,
                "nullConstant tolerates null lookup");
        check(ConstantBootstraps.nullConstant(lookup, null, java.util.List.class) == null,
                "nullConstant tolerates null name");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> ConstantBootstraps.nullConstant(lookup, "n", int.class),
                "not reference: int", "primitive type rejected");
        checkThrows(NullPointerException.class,
                () -> ConstantBootstraps.nullConstant(lookup, "n", null),
                "null type -> NPE");

        // primitiveClass takes a single-character field descriptor, not a type name
        check(ConstantBootstraps.primitiveClass(lookup, "I", Class.class) == int.class,
                "primitiveClass descriptor I");
        check(ConstantBootstraps.primitiveClass(lookup, "J", Class.class) == long.class,
                "primitiveClass descriptor J");
        check(ConstantBootstraps.primitiveClass(lookup, "V", Class.class) == void.class,
                "primitiveClass descriptor V");
        check(ConstantBootstraps.primitiveClass(lookup, "Z", Class.class) == boolean.class,
                "primitiveClass descriptor Z");
        check(ConstantBootstraps.primitiveClass(lookup, "B", Class.class) == byte.class,
                "primitiveClass descriptor B");
        check(ConstantBootstraps.primitiveClass(lookup, "S", Class.class) == short.class,
                "primitiveClass descriptor S");
        check(ConstantBootstraps.primitiveClass(lookup, "C", Class.class) == char.class,
                "primitiveClass descriptor C");
        check(ConstantBootstraps.primitiveClass(lookup, "F", Class.class) == float.class,
                "primitiveClass descriptor F");
        check(ConstantBootstraps.primitiveClass(lookup, "D", Class.class) == double.class,
                "primitiveClass descriptor D");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> ConstantBootstraps.primitiveClass(lookup, "int", Class.class),
                "not primitive: int", "type name instead of descriptor");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> ConstantBootstraps.primitiveClass(lookup, "bytee", Class.class),
                "not primitive: bytee", "misspelled descriptor");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> ConstantBootstraps.primitiveClass(lookup, "Ljava/lang/String;", Class.class),
                "not primitive: Ljava/lang/String;", "reference descriptor rejected");
        checkThrows(NullPointerException.class,
                () -> ConstantBootstraps.primitiveClass(lookup, null, Class.class),
                "null descriptor -> NPE");
        checkThrows(IllegalArgumentException.class,
                () -> ConstantBootstraps.primitiveClass(lookup, "I", String.class),
                "result type must be Class.class");
    }

    // ---- getStaticFinal: values, derived declaring class, failure modes ----
    static void testGetStaticFinalDeep() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        check(ConstantBootstraps.getStaticFinal(lookup, "MAGIC", int.class,
                ConstantBootstrapsTest.class).equals(4242), "own static final int");
        check(ConstantBootstraps.getStaticFinal(lookup, "VERSION", String.class,
                ConstantBootstrapsTest.class).equals("v27"), "own static final String");
        checkThrowsMsg(NoSuchFieldError.class,
                () -> ConstantBootstraps.getStaticFinal(lookup, "NOPE", String.class,
                        ConstantBootstrapsTest.class),
                "does not have member field", "absent field -> NoSuchFieldError");
        checkThrowsMsg(NoSuchFieldError.class,
                () -> ConstantBootstraps.getStaticFinal(lookup, "VERSION", Integer.class,
                        ConstantBootstrapsTest.class),
                "does not have member field", "wrong field type -> NoSuchFieldError");
        checkThrowsMsg(IncompatibleClassChangeError.class,
                () -> ConstantBootstraps.getStaticFinal(lookup, "mutableStatic", String.class,
                        ConstantBootstrapsTest.class),
                "not a final field", "non-final field -> IncompatibleClassChangeError");
        checkThrows(NullPointerException.class,
                () -> ConstantBootstraps.getStaticFinal(null, "VERSION", String.class,
                        ConstantBootstrapsTest.class),
                "null lookup -> NPE");
        checkThrows(NullPointerException.class,
                () -> ConstantBootstraps.getStaticFinal(lookup, null, String.class,
                        ConstantBootstrapsTest.class),
                "null name -> NPE");
        checkThrows(NullPointerException.class,
                () -> ConstantBootstraps.getStaticFinal(lookup, "VERSION", null,
                        ConstantBootstrapsTest.class),
                "null type -> NPE");
        checkThrows(NullPointerException.class,
                () -> ConstantBootstraps.getStaticFinal(lookup, "VERSION", String.class, null),
                "null declaring class -> NPE");
        // 3-arg overload: wrapper class derived for primitives, self type otherwise
        check(ConstantBootstraps.getStaticFinal(lookup, "MAX_VALUE", int.class)
                .equals(Integer.MAX_VALUE), "3-arg derive from wrapper");
        check(ConstantBootstraps.getStaticFinal(lookup, "MIN_VALUE", long.class)
                .equals(Long.MIN_VALUE), "3-arg derive Long wrapper");
        check(ConstantBootstraps.getStaticFinal(lookup, "SPADES", Suit.class) == Suit.SPADES,
                "3-arg self-typed enum constant");
    }

    // ---- invoke BSM form and remaining negatives ----
    static void testInvokeAndVarHandleDeep() throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle deco = lookup.findStatic(ConstantBootstrapsTest.class, "decorate",
                MethodType.methodType(String.class, String.class, int.class));
        check(ConstantBootstraps.invoke(lookup, "x", String.class, deco, "a", 1).equals("a_1"),
                "invoke runs the handle with dynamic args");
        check(ConstantBootstraps.invoke(lookup, "x", CharSequence.class, deco, "b", 2)
                .equals("b_2"), "invoke widens the result type");
        checkThrowsMsg(ClassCastException.class,
                () -> ConstantBootstraps.invoke(lookup, "x", Integer.class, deco, "a", 1),
                "Cannot cast java.lang.String to java.lang.Integer", "invoke cast mismatch");
        checkThrowsMsg(NoSuchFieldError.class,
                () -> ConstantBootstraps.fieldVarHandle(lookup, "NOPE", VarHandle.class,
                        Point.class, int.class),
                "does not have member field", "fieldVarHandle absent field");
        checkThrowsMsg(IllegalArgumentException.class,
                () -> ConstantBootstraps.arrayVarHandle(lookup, "a", VarHandle.class,
                        String.class),
                "not an array", "arrayVarHandle non-array type");
        // explicitCast: null pass-through, primitive conversions
        check(ConstantBootstraps.explicitCast(lookup, "c", int.class, 42).equals(42),
                "explicitCast int identity");
        check(ConstantBootstraps.explicitCast(lookup, "c", long.class, 42).equals(42L),
                "explicitCast int to long");
        check(ConstantBootstraps.explicitCast(lookup, "c", Number.class, 42).equals(42),
                "explicitCast to Number supertype");
        checkThrows(ClassCastException.class,
                () -> ConstantBootstraps.explicitCast(lookup, "c", int.class, "s"),
                "explicitCast string to int -> CCE");
    }

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        testApiSurface();
        testEnumConstantDeep();
        testConstantTypes();
        testGetStaticFinalDeep();
        testInvokeAndVarHandleDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
