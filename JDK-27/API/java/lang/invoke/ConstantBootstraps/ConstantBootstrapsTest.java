/*
 * @test
 * @summary java.lang.invoke.ConstantBootstraps conformance: white-box + black-box behavior.
 * @run main ConstantBootstrapsTest
 */

import java.lang.invoke.ConstantBootstraps;
import java.lang.invoke.MethodHandles;
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

    public static void main(String[] args) throws Throwable {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
