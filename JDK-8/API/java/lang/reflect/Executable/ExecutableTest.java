/*
 * @test
 * @summary java.lang.reflect.Executable conformance: parameters/exceptions/modifiers via a
 *          Method, plus the deepen batch: dual evidence through Method and Constructor
 *          (parameters, annotated return/parameters/exceptions, type parameters with
 *          bounds, exception order, annotation matrix, accessible-flag linkage,
 *          receiver-type evidence per kind, Member/GenericDeclaration contracts,
 *          varargs consistency) and the JDK-8 API surface lock (no accessFlags()).
 * @run main ExecutableTest
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class ExecutableTest {
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

    static class Sample {
        public static void m(int a, String b) throws Exception {
        }

        public static void varargs(String... s) {
        }
    }

    // ---- fixtures added by the deepen batch ----

    static class Ops {
        public int add(int a, int b) throws java.io.IOException, ClassNotFoundException {
            return a + b;
        }

        public static long stat(int x) { return x * 100L; }

        public Ops(int a, @Deprecated String tag) throws java.io.IOException {
        }

        public Ops(int... rest) {
        }

        public <T extends Number> T first(T t) throws java.io.IOException {
            return t;
        }
    }

    static class OpsHolder {
        class OpsIn {
            public OpsIn(int x) {
            }
        }
    }

    static void testExecutable() throws Exception {
        Executable e = Sample.class.getMethod("m", int.class, String.class);
        check(e.getParameterCount() == 2, "getParameterCount");
        check(e.getParameters().length == 2, "getParameters");
        check(e.getParameterTypes().length == 2, "getParameterTypes");
        check("m".equals(e.getName()), "getName");
        check(e.getExceptionTypes().length == 1, "getExceptionTypes");
        check(e.getGenericExceptionTypes().length == 1, "getGenericExceptionTypes");
        check(e.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC), "getModifiers");
        check(e.getDeclaringClass() == Sample.class, "getDeclaringClass");
        check(!e.isVarArgs(), "not varargs");
        check(!e.isSynthetic(), "not synthetic");

        Executable v = Sample.class.getMethod("varargs", String[].class);
        check(v.isVarArgs(), "isVarArgs");
    }

    // ---- tests added by the deepen batch ----

    static void testDualEvidence() throws Exception {
        Executable m = Ops.class.getMethod("add", int.class, int.class);
        Executable c = Ops.class.getConstructor(int.class, String.class);
        check(m.getParameterCount() == 2 && c.getParameterCount() == 2,
                "counts via both kinds");
        check(m.getParameterTypes()[0] == int.class
                && c.getParameterTypes()[1] == String.class, "erased parameter types");
        Parameter[] mp = m.getParameters();
        Parameter[] cp = c.getParameters();
        check(mp.length == 2 && cp.length == 2, "parameter objects length");
        check(mp[0].getType() == int.class && cp[0].getType() == int.class,
                "parameter types align with getParameterTypes");
        check(mp[0].getDeclaringExecutable().equals(m)
                && cp[1].getDeclaringExecutable().equals(c), "parameter back references");
        check(m.getAnnotatedReturnType().getType() == int.class,
                "method annotated return type");
        check(c.getAnnotatedReturnType().getType() == Ops.class,
                "ctor annotated return type is the declaring class");
        check(m.getAnnotatedParameterTypes().length == 2
                && c.getAnnotatedParameterTypes()[0].getType() == int.class,
                "annotated parameter types");
    }

    static void testTypeParametersView() throws Exception {
        Executable add = Ops.class.getMethod("add", int.class, int.class);
        check(add.getTypeParameters().length == 0, "non-generic method has no TVs");
        Executable first = Ops.class.getMethod("first", Number.class);
        TypeVariable<?>[] tvs = first.getTypeParameters();
        check(tvs.length == 1 && "T".equals(tvs[0].getName()),
                "generic method type parameter T");
        check(tvs[0].getBounds()[0] == Number.class, "T bound Number");
        check(first.getGenericParameterTypes()[0] instanceof TypeVariable,
                "generic parameter TypeVariable");
        Executable c = Ops.class.getConstructor(int.class, String.class);
        check(c.getTypeParameters().length == 0, "non-generic ctor has no TVs");
        check(first instanceof java.lang.reflect.GenericDeclaration,
                "Executable implements GenericDeclaration");
    }

    static void testExceptionTypesView() throws Exception {
        Executable add = Ops.class.getMethod("add", int.class, int.class);
        Class<?>[] ex = add.getExceptionTypes();
        check(ex.length == 2 && ex[0] == java.io.IOException.class
                && ex[1] == ClassNotFoundException.class, "exception order exact");
        Type[] gex = add.getGenericExceptionTypes();
        check(gex.length == 2 && gex[0] == java.io.IOException.class,
                "generic exception view");
        check(add.getAnnotatedExceptionTypes().length == 2,
                "annotated exceptions length");
        check(add.getAnnotatedExceptionTypes()[1].getType() == ClassNotFoundException.class,
                "annotated exception type");
    }

    static void testAnnotationMatrix() throws Exception {
        Executable c = Ops.class.getConstructor(int.class, String.class);
        Annotation[][] pa = c.getParameterAnnotations();
        check(pa.length == 2, "parameter annotation rows");
        check(pa[0].length == 0, "unannotated parameter row empty");
        check(pa[1].length == 1 && pa[1][0] instanceof Deprecated,
                "annotated parameter row");
        check(c.getDeclaredAnnotations().length == 0, "no ctor annotations");
        check(c.getAnnotation(Deprecated.class) == null, "absent ctor annotation null");
        check(!c.isAnnotationPresent(Deprecated.class),
                "absent isAnnotationPresent false");
        Executable m = Ops.class.getMethod("add", int.class, int.class);
        check(m.getParameterAnnotations().length == 2
                && m.getParameterAnnotations()[0].length == 0,
                "method parameter annotation rows");
    }

    static void testAccessibleFlagLinkage() throws Exception {
        Executable m = Ops.class.getMethod("add", int.class, int.class);
        Executable c = Ops.class.getConstructor(int.class, String.class);
        check(!m.isAccessible() && !c.isAccessible(), "both default inaccessible");
        m.setAccessible(true);
        check(m.isAccessible() && !c.isAccessible(), "single flag independent");
        AccessibleObject[] batch = {m, c};
        AccessibleObject.setAccessible(batch, true);
        check(m.isAccessible() && c.isAccessible(), "batch setAccessible true");
        AccessibleObject.setAccessible(batch, false);
        check(!m.isAccessible() && !c.isAccessible(), "batch setAccessible false");
    }

    static void testReceiverTypeEvidence() throws Exception {
        Executable add = Ops.class.getMethod("add", int.class, int.class);
        Executable stat = Ops.class.getMethod("stat", int.class);
        check(add.getAnnotatedReceiverType() != null,
                "instance method receiver non-null");
        check(stat.getAnnotatedReceiverType() == null,
                "static method receiver null");
        Executable inner = OpsHolder.OpsIn.class
                .getConstructor(OpsHolder.class, int.class);
        check(inner.getAnnotatedReceiverType() != null
                && inner.getAnnotatedReceiverType().getType() == OpsHolder.class,
                "inner ctor receiver is the enclosing class");
        Executable statNest = Ops.class.getConstructor(int.class, String.class);
        // JDK-8 quirk: static-nested ctor reports a degenerate non-null receiver
        // (fixed in JDK-9) — locked version difference.
        check(statNest.getAnnotatedReceiverType() != null,
                "JDK-8 static-nested ctor receiver non-null");
    }

    static void testMemberContract() throws Exception {
        Executable m = Ops.class.getMethod("add", int.class, int.class);
        check(m instanceof Member, "Executable implements Member");
        Member mm = m;
        check("add".equals(mm.getName()), "Member name");
        check(mm.getDeclaringClass() == Ops.class, "Member declaring class");
        check(mm.getModifiers() == Modifier.PUBLIC, "Member modifiers");
        check(!mm.isSynthetic(), "Member not synthetic");
        Executable c = Ops.class.getConstructor(int.class, String.class);
        check(c.getName().equals(Ops.class.getName()),
                "ctor Member name is the class name");
    }

    static void testVarargsConsistency() throws Exception {
        Executable v = Ops.class.getConstructor(int[].class);
        check(v.isVarArgs(), "varargs ctor flag");
        check(v.getParameterTypes()[0] == int[].class, "varargs parameter array type");
        check(v.getParameters()[0].isVarArgs(), "Parameter varargs flag");
        Executable plain = Ops.class.getConstructor(int.class, String.class);
        check(!plain.isVarArgs() && !plain.getParameters()[0].isVarArgs(),
                "plain flags");
        check(v.toGenericString().contains("Ops"), "toGenericString contains class");
        check(plain.toGenericString().contains("(int,java.lang.String)"),
                "toGenericString signature");
    }

    static void testApiSurfaceLocked() throws Exception {
        // Executable gained accessFlags() only in JDK 20 — absent on JDK-8.
        checkThrows(NoSuchMethodException.class,
                () -> Executable.class.getMethod("accessFlags"),
                "no accessFlags on JDK-8 Executable");
    }

    public static void main(String[] args) throws Exception {
        testExecutable();
        testDualEvidence();
        testTypeParametersView();
        testExceptionTypesView();
        testAnnotationMatrix();
        testAccessibleFlagLinkage();
        testReceiverTypeEvidence();
        testMemberContract();
        testVarargsConsistency();
        testApiSurfaceLocked();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
