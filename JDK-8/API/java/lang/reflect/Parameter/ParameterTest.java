/*
 * @test
 * @summary java.lang.reflect.Parameter conformance: type/name/declaring executable/varargs,
 *          plus the deepen batch: locked argN naming under default compilation (no
 *          -parameters), varargs position sensitivity, generic parameter views
 *          (TypeVariable / ParameterizedType), annotation accessors, equals/hashCode
 *          stability and the inner-constructor implicit-parameter evidence
 *          (JDK-8 reports non-implicit without -parameters).
 * @run main ParameterTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;

public class ParameterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public void m(int a, String b) {
        }

        public void varargs(String... s) {
        }
    }

    // ---- fixtures added by the deepen batch ----

    static class POver {
        public void two(int a, String b) {
        }

        public void varMixed(int fixed, int... rest) {
        }

        public <T extends Number> void gen(T t) {
        }

        public void pl(java.util.List<String> xs) {
        }

        public void marked(int a, @Deprecated Object o) {
        }
    }

    static class PHolder {
        class PIn {
            public PIn(int x) {
            }
        }
    }

    static void testParameter() throws Exception {
        Method m = Sample.class.getMethod("m", int.class, String.class);
        Parameter[] params = m.getParameters();
        check(params.length == 2, "getParameters length");
        Parameter p0 = params[0];
        check(p0.getType() == int.class, "getType");
        check(p0.getParameterizedType() == int.class, "getParameterizedType");
        check(p0.getName() != null, "getName non-null");
        check(p0.getModifiers() == 0, "getModifiers");
        check(p0.getDeclaringExecutable() == m, "getDeclaringExecutable");
        check(p0.getAnnotations().length == 0, "getAnnotations");
        check(!p0.isSynthetic(), "isSynthetic false");
        check(!p0.isVarArgs(), "isVarArgs false");
        check(p0.toString() != null, "toString");

        Method v = Sample.class.getMethod("varargs", String[].class);
        check(v.getParameters()[0].isVarArgs(), "varargs parameter");
    }

    // ---- tests added by the deepen batch ----

    static void testNameAbsenceContract() throws Exception {
        // jtreg compiles without -parameters, so real names are absent and the
        // synthesized argN form is locked (verified on JDK-8).
        Method m = POver.class.getMethod("two", int.class, String.class);
        Parameter[] ps = m.getParameters();
        check(!ps[0].isNamePresent() && !ps[1].isNamePresent(),
                "names absent without -parameters");
        check("arg0".equals(ps[0].getName()), "synthesized name arg0");
        check("arg1".equals(ps[1].getName()), "synthesized name arg1");
        check("int arg0".equals(ps[0].toString()), "toString is 'int arg0'");
        check(ps[1].toString().contains("arg1"), "toString contains arg1");
    }

    static void testVarargsPosition() throws Exception {
        Method m = POver.class.getMethod("varMixed", int.class, int[].class);
        Parameter[] ps = m.getParameters();
        check(ps.length == 2, "two parameters");
        check(!ps[0].isVarArgs(), "fixed parameter not varargs");
        check(ps[1].isVarArgs(), "trailing parameter is varargs");
        check(ps[1].getType() == int[].class, "varargs parameter array type");
        check(!ps[1].isImplicit() && !ps[1].isSynthetic(),
                "source varargs parameter plain flags");
    }

    static void testGenericParameterViews() throws Exception {
        Method g = POver.class.getMethod("gen", Number.class);
        Parameter p = g.getParameters()[0];
        check(p.getType() == Number.class, "erased type is the bound Number");
        check(p.getParameterizedType() instanceof TypeVariable,
                "generic parameter TypeVariable");
        check("T".equals(p.getParameterizedType().getTypeName()),
                "type variable name T");
        Method l = POver.class.getMethod("pl", java.util.List.class);
        Parameter lp = l.getParameters()[0];
        check(lp.getType() == java.util.List.class, "List raw type");
        check(lp.getParameterizedType() instanceof ParameterizedType,
                "List parameter is ParameterizedType");
        check(lp.getParameterizedType().getTypeName()
                .equals("java.util.List<java.lang.String>"), "List type name");
        check(p.getAnnotatedType().getType() instanceof TypeVariable,
                "annotated type keeps the type variable");
    }

    static void testEqualsHashCodeContract() throws Exception {
        Method m = POver.class.getMethod("two", int.class, String.class);
        Parameter[] a = m.getParameters();
        Parameter[] b = m.getParameters();
        check(a[0].equals(b[0]), "same parameter equals across lookups");
        check(a[0].hashCode() == b[0].hashCode(), "same hashCode");
        check(!a[0].equals(a[1]), "different positions unequal");
        check(!a[0].equals(null), "not equal to null");
        check(!a[0].equals("arg0"), "not equal to a foreign object");
        check(a[0].getDeclaringExecutable().equals(m), "declaring executable");
    }

    static void testAnnotationOnParameter() throws Exception {
        Method m = POver.class.getMethod("marked", int.class, Object.class);
        Parameter[] ps = m.getParameters();
        check(ps[0].getAnnotation(Deprecated.class) == null,
                "unannotated parameter null");
        check(ps[1].getAnnotation(Deprecated.class) != null, "annotated parameter found");
        check(ps[1].getAnnotations().length == 1, "annotations length one");
        check(ps[1].isAnnotationPresent(Deprecated.class), "isAnnotationPresent");
        check(ps[1].getDeclaredAnnotation(Deprecated.class) != null,
                "getDeclaredAnnotation");
        check(ps[0].getDeclaredAnnotations().length == 0,
                "unannotated declared empty");
    }

    static void testInnerCtorParameters() throws Exception {
        Constructor<?> c = PHolder.PIn.class
                .getConstructor(PHolder.class, int.class);
        Parameter[] ps = c.getParameters();
        check(ps.length == 2, "inner ctor has two parameters");
        check(ps[0].getType() == PHolder.class,
                "first parameter is the enclosing instance");
        // JDK-8 cannot derive the implicit flag without the MethodParameters
        // attribute; JDK-9+ derives it from the enclosing-class signature —
        // locked version difference.
        check(!ps[0].isImplicit(),
                "JDK-8 reports non-implicit without -parameters");
        check(ps[0].getModifiers() == 0, "outer-instance parameter modifiers zero");
        check(ps[1].getType() == int.class && !ps[1].isImplicit(),
                "declared parameter plain");
        check("arg1".equals(ps[1].getName()), "ctor parameter synthesized name");
        check(ps[1].getDeclaringExecutable().equals(c), "ctor back reference");
    }

    public static void main(String[] args) throws Exception {
        testParameter();
        testNameAbsenceContract();
        testVarargsPosition();
        testGenericParameterViews();
        testEqualsHashCodeContract();
        testAnnotationOnParameter();
        testInnerCtorParameters();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
