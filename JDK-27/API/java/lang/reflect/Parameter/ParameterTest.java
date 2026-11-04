/*
 * @test
 * @summary java.lang.reflect.Parameter conformance: white-box structure + black-box behavior,
 *          plus the deepen batch: locked argN naming under default compilation, varargs
 *          position sensitivity, ctor-parameter views and the inner-constructor implicit
 *          parameter flag bits (FINAL|MANDATED) with their accessFlags() mapping.
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

        public void mixed(int fixed, @Deprecated Object marked) {
        }
    }

    static class Generics {
        public <T> void single(T t) {
        }

        public void list(java.util.List<String> xs) {
        }
    }

    // non-static inner class: its constructor's first parameter is implicit
    static class Outer {
        class Inner {
            Inner() {
            }
        }
    }

    // ---- fixtures added by the deepen batch ----

    static class POver {
        public void varMixed(int fixed, int... rest) {
        }
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
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

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. position, type, erased and generic types
        Method m = Sample.class.getMethod("m", int.class, String.class);
        Parameter[] ps = m.getParameters();
        check(ps[0].getType() == int.class && ps[1].getType() == String.class, "types by position");
        check(ps[0].getParameterizedType() == int.class
              && ps[1].getParameterizedType() == String.class, "erased parameterized types");
        check(ps[0].isNamePresent() == ps[1].isNamePresent(), "name presence consistent");
        if (!ps[0].isNamePresent()) {
            check(ps[0].getName().equals("arg0") && ps[1].getName().equals("arg1"),
                  "synthesized names argN when not present");
        } else {
            check(ps[0].getName().equals("a") && ps[1].getName().equals("b"),
                  "real names when present");
        }
        check(ps[0].toString().contains("int"), "toString contains type");

        // 2. varargs position sensitivity
        Method varm = Sample.class.getMethod("varargs", String[].class);
        Parameter vp = varm.getParameters()[0];
        check(vp.isVarArgs(), "trailing varargs param true");
        check(vp.getType() == String[].class, "varargs param type is array");

        // 3. annotations on parameters
        Method mixed = Sample.class.getMethod("mixed", int.class, Object.class);
        Parameter[] mps = mixed.getParameters();
        check(mps[0].getAnnotation(Deprecated.class) == null, "unannotated param null");
        check(mps[1].getAnnotation(Deprecated.class) != null, "annotated param Deprecated");
        check(mps[1].getAnnotations().length == 1, "annotation count");
        check(mps[1].isAnnotationPresent(Deprecated.class), "isAnnotationPresent");
        check(mps[1].getDeclaredAnnotation(Deprecated.class) != null, "getDeclaredAnnotation");
        check(mps[1].getAnnotatedType().getType() == Object.class, "annotated type erased");

        // 4. generic parameter types
        Method single = Generics.class.getMethod("single", Object.class);
        Parameter sp = single.getParameters()[0];
        check(sp.getType() == Object.class, "generic param erased to Object");
        check(sp.getParameterizedType() instanceof TypeVariable, "generic param TypeVariable");
        check("T".equals(sp.getParameterizedType().getTypeName()), "type variable name T");
        Method list = Generics.class.getMethod("list", java.util.List.class);
        Parameter lp = list.getParameters()[0];
        check(lp.getParameterizedType() instanceof ParameterizedType, "List param is PT");
        check(lp.getParameterizedType().getTypeName().equals("java.util.List<java.lang.String>"),
              "List param typeName");

        // 5. implicit parameter of a non-static inner class constructor
        Constructor<Outer.Inner> ic = (Constructor<Outer.Inner>) (Constructor<?>) Outer.Inner.class.getDeclaredConstructor(Outer.class);
        Parameter[] icps = ic.getParameters();
        check(icps.length == 1, "inner ctor single param");
        check(icps[0].isImplicit(), "first param implicit");
        check(!icps[0].isSynthetic(), "implicit but not synthetic");
        check(icps[0].getType() == Outer.class, "implicit param type is enclosing class");
        check(icps[0].getModifiers() == 0x8000 || !java.lang.reflect.Modifier.isPublic(icps[0].getModifiers()),
              "implicit param not public");

        // 6. equality and modifiers
        Parameter[] again = m.getParameters();
        check(ps[0].equals(again[0]) && ps[0].hashCode() == again[0].hashCode(),
              "Parameter equals/hashCode stable");
        check(!ps[0].equals(ps[1]), "distinct params not equal");
        check(!ps[0].equals(null) && !ps[0].equals("x"), "not equal to foreign objects");
        check(ps[0].getModifiers() == 0 && ps[1].getModifiers() == 0, "plain params zero modifiers");
        check(ps[0].getDeclaringExecutable().equals(m), "declaring executable");
        check(!ps[0].isImplicit() && !ps[0].isSynthetic(), "source params plain flags");
        check(!vp.isSynthetic() && !vp.isImplicit(), "varargs param plain flags");
        check(ps[0].accessFlags().isEmpty(), "no access flags on plain param");
    }

    // ---- tests added by the deepen batch ----

    static void testNameAbsenceContract() throws Exception {
        // jtreg compiles without -parameters, so the argN synthesized names are the
        // deterministic contract on this toolchain (verified on JDK-27).
        Method m = Sample.class.getMethod("m", int.class, String.class);
        Parameter[] ps = m.getParameters();
        check(!ps[0].isNamePresent(), "name absent without -parameters");
        check("arg0".equals(ps[0].getName()) && "arg1".equals(ps[1].getName()),
                "synthesized names arg0/arg1");
        check("int arg0".equals(ps[0].toString()), "toString is 'int arg0'");
    }

    static void testVarargsPosition() throws Exception {
        Method m = POver.class.getMethod("varMixed", int.class, int[].class);
        Parameter[] ps = m.getParameters();
        check(ps.length == 2, "two parameters");
        check(!ps[0].isVarArgs(), "fixed parameter not varargs");
        check(ps[1].isVarArgs(), "trailing parameter is varargs");
        check(ps[1].getType() == int[].class, "varargs parameter array type");
        check(ps[1].getParameterizedType() == int[].class,
                "varargs parameterized type is the array class");
    }

    static void testCtorParameterViews() throws Exception {
        Constructor<Outer.Inner> ic = (Constructor<Outer.Inner>) (Constructor<?>) Outer.Inner.class.getDeclaredConstructor(Outer.class);
        Parameter[] ps = ic.getParameters();
        check("arg0".equals(ps[0].getName()), "ctor parameter synthesized name");
        check(ps[0].getDeclaringExecutable().equals(ic), "ctor back reference");
        check(ps[0].toString().contains("arg0"), "ctor toString contains arg0");
    }

    static void testInnerCtorParamFlagsLocked() throws Exception {
        Constructor<?> ic = Outer.Inner.class.getDeclaredConstructor(Outer.class);
        Parameter p0 = ic.getParameters()[0];
        // JDK-9+ derives the implicit outer-instance parameter: modifiers carry the
        // mandated bit (0x8000) plus final (0x10) — locked version difference vs
        // JDK-8 (which reported 0 / non-implicit without -parameters).
        check(p0.getModifiers() == 0x8010,
                "implicit parameter modifiers FINAL(0x10)|MANDATED(0x8000)");
        java.util.Set<java.lang.reflect.AccessFlag> af = p0.accessFlags();
        check(af.size() == 2 && af.contains(java.lang.reflect.AccessFlag.FINAL)
                && af.contains(java.lang.reflect.AccessFlag.MANDATED),
                "implicit parameter accessFlags {FINAL, MANDATED}");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNameAbsenceContract();
        testVarargsPosition();
        testCtorParameterViews();
        testInnerCtorParamFlagsLocked();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
