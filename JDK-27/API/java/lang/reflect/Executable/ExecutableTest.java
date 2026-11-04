/*
 * @test
 * @summary java.lang.reflect.Executable conformance: white-box structure + black-box behavior.
 * @run main ExecutableTest
 */

import java.lang.reflect.AccessFlag;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

    interface WithDefault {
        default String hi() {
            return "hi";
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
