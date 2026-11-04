/*
 * @test
 * @summary java.lang.reflect.Executable conformance: white-box structure + black-box behavior,
 *          plus the deepen batch: dual evidence through Method and Constructor (parameters,
 *          annotated return/parameters/exceptions, exception order, annotation matrix,
 *          accessible-flag linkage, receiver-type evidence per kind, Member/GenericDeclaration
 *          contracts, varargs consistency) and the accessFlags() contract (JDK 20+).
 * @run main ExecutableTest
 */

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class ExecutableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public static void m(int a, String b) throws Exception {
        }

        public static void varargs(String... s) {
        }
    }

    static class Rich {
        public <T extends Number> T pick(T t) throws java.io.IOException, IllegalStateException {
            return t;
        }

        public Rich(int x) {
        }

        public Rich(String... parts) {
        }
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
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
        check(Modifier.isAbstract(Executable.class.getModifiers())
              && Modifier.isPublic(Executable.class.getModifiers()), "Executable is public abstract");
        check(Executable.class.getSuperclass()
              .getName().equals("java.lang.reflect.AccessibleObject"), "extends AccessibleObject");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. generic method viewed through the Executable contract
        Executable pick = Rich.class.getMethod("pick", Number.class);
        TypeVariable<?>[] tvs = pick.getTypeParameters();
        check(tvs.length == 1 && "T".equals(tvs[0].getName()), "method type parameter T");
        check(tvs[0].getBounds()[0] == Number.class, "T bound Number");
        check(pick.getGenericParameterTypes().length == 1
              && pick.getGenericParameterTypes()[0] instanceof TypeVariable,
              "generic parameter type");
        check(pick.getAnnotatedReturnType().getType() instanceof TypeVariable,
              "annotated return keeps TV");
        check(pick.getAnnotatedParameterTypes().length == 1, "annotated parameter count");
        check(pick.getAnnotatedParameterTypes()[0].getType() instanceof TypeVariable
              && "T".equals(pick.getAnnotatedParameterTypes()[0].getType().getTypeName()),
              "annotated parameter keeps type variable");
        check(pick.toGenericString().contains("<T extends java.lang.Number>"),
              "toGenericString shows type parameter and bound");

        // 2. exception views: exact classes and generic form
        Class<?>[] exs = pick.getExceptionTypes();
        check(exs.length == 2 && exs[0] == java.io.IOException.class
              && exs[1] == IllegalStateException.class, "declared exceptions exact");
        Type[] gexs = pick.getGenericExceptionTypes();
        check(gexs.length == 2 && gexs[0] == java.io.IOException.class,
              "generic exception types");

        // 3. constructor path through the same contract
        Executable ctor = Rich.class.getConstructor(int.class);
        check(ctor.getName().equals(Rich.class.getName()), "ctor name is class name");
        check(ctor.getParameterCount() == 1 && ctor.getParameterTypes()[0] == int.class,
              "ctor parameter views");
        check(!ctor.isVarArgs(), "plain ctor not varargs");
        check(ctor.getAnnotatedReturnType().getType() == Rich.class,
              "ctor annotated return is declaring class");
        Executable vaCtor = Rich.class.getConstructor(String[].class);
        check(vaCtor.isVarArgs(), "varargs ctor flag via Executable");
        check(vaCtor.getParameterTypes()[0] == String[].class, "varargs ctor param type");
        check(vaCtor.getParameters()[0].isVarArgs(), "varargs ctor parameter flag");

        // 4. parameters and access flags on both kinds
        check(pick.getParameters().length == 1, "method parameter objects");
        check(pick.getParameters()[0].getDeclaringExecutable().equals(pick),
              "parameter back-reference");
        java.util.Set<AccessFlag> mflags = pick.accessFlags();
        check(mflags.contains(AccessFlag.PUBLIC) && mflags.size() == 1,
              "method access flags exact set");
        java.util.Set<AccessFlag> cflags = ctor.accessFlags();
        check(cflags.contains(AccessFlag.PUBLIC), "ctor access flags public");

        // 5. parameter annotations matrix
        check(pick.getParameterAnnotations().length == 1, "method param annotation rows");
        check(pick.getParameterAnnotations()[0].length == 0, "no annotations on unannotated");
        check(ctor.getParameterAnnotations().length == 1, "ctor param annotation rows");

        // 6. default methods and synthetic flags
        check(!pick.isSynthetic(), "source method not synthetic");
        Method def = WithDefault.class.getMethod("hi");
        check(def.isDefault(), "interface default via Method (Executable subtype)");
        Executable stat = Sample.class.getMethod("m", int.class, String.class);
        check(Modifier.isStatic(stat.getModifiers()), "static flag via modifiers");

        // 7. equality across the supertype reference
        Method mAgain = Rich.class.getMethod("pick", Number.class);
        check(pick.equals(mAgain) && pick.hashCode() == mAgain.hashCode(),
              "Executable view equals Method value");
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

    interface WithDefault {
        default String hi() {
            return "hi";
        }
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
        // JDK-9+ correctly returns null for a static-nested ctor (JDK-8 returned a
        // degenerate non-null value) — locked version difference.
        check(statNest.getAnnotatedReceiverType() == null,
                "JDK-9+ static-nested ctor receiver null");
    }

    static void testMemberContract() throws Exception {
        Executable m = Ops.class.getMethod("add", int.class, int.class);
        check(m instanceof java.lang.reflect.Member, "Executable implements Member");
        java.lang.reflect.Member mm = m;
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

    static void testAccessFlagsContract() throws Exception {
        // accessFlags() (JDK 20+) is present on JDK-27 and mirrors getModifiers().
        Executable sm = Sample.class.getMethod("m", int.class, String.class);
        java.util.Set<AccessFlag> f = sm.accessFlags();
        check(f.contains(AccessFlag.PUBLIC) && f.contains(AccessFlag.STATIC)
                && f.size() == 2, "static public method flags exact set");
        Executable add = Ops.class.getMethod("add", int.class, int.class);
        check(add.accessFlags().size() == 1
                && add.accessFlags().contains(AccessFlag.PUBLIC),
                "instance public method flags");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDualEvidence();
        testExceptionTypesView();
        testAnnotationMatrix();
        testAccessibleFlagLinkage();
        testReceiverTypeEvidence();
        testMemberContract();
        testVarargsConsistency();
        testAccessFlagsContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
