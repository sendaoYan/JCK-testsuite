/*
 * @test
 * @summary java.lang.reflect.Method conformance: full contract coverage.
 *          return/parameter/exception type reflection incl. generic signatures and
 *          type parameters, invoke instance/static with null semantics, wrong
 *          arity/type -> IllegalArgumentException, private access without setAccessible
 *          -> IllegalAccessException, InvocationTargetException wraps the exact cause
 *          (getCause/getTargetException identity), isVarArgs/isBridge/isSynthetic/
 *          isDefault, annotation-element getDefaultValue, getAnnotation/
 *          getDeclaredAnnotations/getParameterAnnotations/getAnnotatedReturnType,
 *          equals/hashCode/toString/toGenericString, and getMethods vs getDeclaredMethods
 *          inheritance semantics + NoSuchMethodException / NPE paths.
 * @run main MethodTest
 */

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

public class MethodTest {
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

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface MAnn { String value(); }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface PAnn { String value(); }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Ann {
        String name() default "dflt";
        int num() default 42;
        String noDefault();
    }

    static class Sample {
        public int add(int a, int b) { return a + b; }
        private int secret(int x) { return x * 2; }
        public static int stat(int x) { return x * 10; }
        public int thrower() throws IOException { throw new IOException("boom"); }
        public int sum(int... nums) { int t = 0; for (int n : nums) t += n; return t; }
        public <T> T identity(T in) { return in; }
        public void param(@PAnn("px") int x, String y) { }
        @MAnn("m") public int annotated() { return 1; }
        public int declaresChecked() throws IOException, IllegalStateException { return 0; }
    }

    static class GenericBase<T> {
        public T echo(T t) { return t; }
    }

    static class StringImpl extends GenericBase<String> {
        public String echo(String s) { return s; }
    }

    interface WithDefault {
        default int dflt() { return 7; }
        int abstractM();
    }

    static void testMetadata() throws Exception {
        Method m = Sample.class.getMethod("add", int.class, int.class);
        check("add".equals(m.getName()), "getName");
        check(m.getReturnType() == int.class, "getReturnType");
        check(m.getGenericReturnType() == int.class, "getGenericReturnType primitive");
        check(m.getParameterTypes().length == 2, "getParameterTypes length");
        check(m.getParameterTypes()[0] == int.class && m.getParameterTypes()[1] == int.class,
                "getParameterTypes elements");
        check(m.getParameterCount() == 2, "getParameterCount");
        check(m.getGenericParameterTypes().length == 2, "getGenericParameterTypes length");
        check(m.getDeclaringClass() == Sample.class, "getDeclaringClass");
        check(!Modifier.isStatic(m.getModifiers()), "instance method not static");
        check(Modifier.isPublic(m.getModifiers()), "public method");
    }

    static void testGenericSignature() throws Exception {
        Method m = Sample.class.getMethod("identity", Object.class);
        check(m.getReturnType() == Object.class, "generic erasure return type");
        check(m.getGenericReturnType() instanceof TypeVariable,
                "generic return is TypeVariable");
        TypeVariable<?>[] tp = m.getTypeParameters();
        check(tp.length == 1, "one type parameter");
        check(tp[0].getName().equals("T"), "type parameter name");
        check(m.getGenericParameterTypes()[0] instanceof TypeVariable,
                "generic parameter is TypeVariable");
    }

    static void testExceptionTypes() throws Exception {
        Method m = Sample.class.getMethod("thrower");
        Class<?>[] ex = m.getExceptionTypes();
        check(ex.length == 1 && ex[0] == IOException.class, "getExceptionTypes");
        check(m.getGenericExceptionTypes().length == 1, "getGenericExceptionTypes length");

        Method dc = Sample.class.getMethod("declaresChecked");
        Class<?>[] ex2 = dc.getExceptionTypes();
        check(ex2.length == 2, "declaresChecked two exceptions");
        check(ex2[0] == IOException.class && ex2[1] == IllegalStateException.class,
                "declaresChecked exception order");
    }

    static void testInvoke() throws Exception {
        Sample s = new Sample();
        Method add = Sample.class.getMethod("add", int.class, int.class);
        check(add.invoke(s, 2, 3).equals(5), "invoke instance");
        check(add.invoke(s, Integer.valueOf(-1), Integer.valueOf(5)).equals(4),
                "invoke boxed widening");

        Method stat = Sample.class.getMethod("stat", int.class);
        check(stat.invoke(null, 5).equals(50), "invoke static null receiver");
        check(stat.invoke(s, 6).equals(60), "invoke static with receiver ignored");

        // wrong arity -> IllegalArgumentException
        checkThrows(IllegalArgumentException.class,
                () -> add.invoke(s, 1), "invoke too few args IAE");
        checkThrows(IllegalArgumentException.class,
                () -> add.invoke(s, 1, 2, 3), "invoke too many args IAE");
        checkThrows(IllegalArgumentException.class,
                () -> add.invoke(s, (Object[]) null), "invoke null args IAE");
        // wrong argument type -> IllegalArgumentException
        checkThrows(IllegalArgumentException.class,
                () -> add.invoke(s, 1.0, 2), "invoke wrong arg type IAE");
        // null receiver on instance method -> NullPointerException
        checkThrows(NullPointerException.class,
                () -> add.invoke(null, 1, 2), "invoke instance null receiver NPE");
    }

    static void testInvocationTargetException() throws Exception {
        Sample s = new Sample();
        Method thrower = Sample.class.getMethod("thrower");
        try {
            thrower.invoke(s);
            throw new AssertionError("expected InvocationTargetException");
        } catch (InvocationTargetException ite) {
            passed++;
            check(ite.getCause() instanceof IOException, "getCause is IOException");
            check(ite.getTargetException() instanceof IOException,
                    "getTargetException is IOException");
            check(ite.getTargetException() == ite.getCause(),
                    "getTargetException == getCause identity");
            check("boom".equals(ite.getTargetException().getMessage()),
                    "original message preserved");
        }

        // InvocationTargetException itself can be constructed and carries cause/target
        IOException cause = new IOException("c");
        InvocationTargetException ite = new InvocationTargetException(cause);
        check(ite.getTargetException() == cause, "constructor sets targetException");
        check(ite.getCause() == cause, "constructor sets cause");
    }

    static void testAccessibility() throws Exception {
        Sample s = new Sample();
        Method secret = Sample.class.getDeclaredMethod("secret", int.class);
        check(Modifier.isPrivate(secret.getModifiers()), "secret private");
        checkThrows(IllegalAccessException.class, () -> secret.invoke(s, 4),
                "private invoke without accessible IAE");
        secret.setAccessible(true);
        check(secret.invoke(s, 4).equals(8), "private invoke after setAccessible");
    }

    static void testVarArgs() throws Exception {
        Method sum = Sample.class.getMethod("sum", int[].class);
        check(sum.isVarArgs(), "sum isVarArgs");
        Sample s = new Sample();
        check(sum.invoke(s, new Object[]{new int[]{1, 2, 3}}).equals(6),
                "varargs invoke with array");
        check(sum.invoke(s, (Object) new int[]{1, 2, 3, 4}).equals(10),
                "varargs invoke boxed array");

        Method add = Sample.class.getMethod("add", int.class, int.class);
        check(!add.isVarArgs(), "add not varargs");
    }

    static void testBridgeSynthetic() throws Exception {
        Method bridge = null;
        for (Method m : StringImpl.class.getMethods()) {
            if (m.getName().equals("echo") && m.isBridge()) {
                bridge = m;
            }
        }
        check(bridge != null, "StringImpl has bridge method");
        check(bridge.isBridge(), "bridge isBridge");
        check(bridge.isSynthetic(), "bridge isSynthetic");
        check(bridge.getParameterTypes().length == 1
                && bridge.getParameterTypes()[0] == Object.class,
                "bridge erasure parameter is Object");
        check(bridge.getReturnType() == Object.class, "bridge erasure return Object");

        Method real = StringImpl.class.getMethod("echo", String.class);
        check(!real.isBridge(), "real echo not bridge");
    }

    static void testDefaultMethod() throws Exception {
        Method d = WithDefault.class.getMethod("dflt");
        check(d.isDefault(), "default method isDefault");
        Method a = WithDefault.class.getMethod("abstractM");
        check(!a.isDefault(), "abstract interface method not default");
        check(Modifier.isAbstract(a.getModifiers()), "abstract method abstract flag");

        Method add = Sample.class.getMethod("add", int.class, int.class);
        check(!add.isDefault(), "class method not default");
    }

    static void testAnnotationDefaultValue() throws Exception {
        check("dflt".equals(Ann.class.getMethod("name").getDefaultValue()),
                "getDefaultValue string");
        check(Ann.class.getMethod("num").getDefaultValue().equals(42),
                "getDefaultValue int");
        check(Ann.class.getMethod("noDefault").getDefaultValue() == null,
                "no default -> null");

        Method add = Sample.class.getMethod("add", int.class, int.class);
        check(add.getDefaultValue() == null, "non-annotation method default null");
    }

    static void testAnnotations() throws Exception {
        Method am = Sample.class.getMethod("annotated");
        MAnn mann = am.getAnnotation(MAnn.class);
        check(mann != null && "m".equals(mann.value()), "getAnnotation");

        Annotation[] declared = am.getDeclaredAnnotations();
        boolean found = false;
        for (Annotation a : declared) {
            if (a instanceof MAnn) found = true;
        }
        check(found, "getDeclaredAnnotations includes MAnn");
        check(am.getAnnotatedReturnType() != null, "getAnnotatedReturnType non-null");

        // parameter annotations
        Method pm = Sample.class.getMethod("param", int.class, String.class);
        check(pm.getParameterAnnotations().length == 2, "getParameterAnnotations length");
        check(pm.getParameterAnnotations()[0].length == 1
                && pm.getParameterAnnotations()[0][0] instanceof PAnn,
                "parameter annotation present");
        check(pm.getParameterAnnotations()[1].length == 0,
                "unannotated parameter empty");

        // unannotated method
        check(Sample.class.getMethod("add", int.class, int.class).getAnnotation(MAnn.class) == null,
                "unannotated getAnnotation null");
    }

    static void testEqualsHashCodeToString() throws Exception {
        Method a = Sample.class.getMethod("add", int.class, int.class);
        Method b = Sample.class.getMethod("add", int.class, int.class);
        Method c = Sample.class.getMethod("stat", int.class);
        check(a.equals(b), "equals same method");
        check(a.hashCode() == b.hashCode(), "hashCode same");
        check(!a.equals(c), "equals different method");
        check(!a.equals(null), "equals null");
        check(!a.equals("add"), "equals non-Method");
        check(a.toString().contains("add"), "toString contains name");
        check(a.toGenericString().contains("int"), "toGenericString contains type");
    }

    static void testDiscovery() throws Exception {
        // getMethods includes inherited public Object methods
        Method[] all = Sample.class.getMethods();
        boolean hasObjectMethod = false, hasAdd = false, hasSecret = false;
        for (Method m : all) {
            if (m.getDeclaringClass() == Object.class) hasObjectMethod = true;
            if (m.getName().equals("add")) hasAdd = true;
            if (m.getName().equals("secret")) hasSecret = true;
        }
        check(hasObjectMethod, "getMethods includes Object methods");
        check(hasAdd, "getMethods includes add");
        check(!hasSecret, "getMethods excludes private");

        Method[] declared = Sample.class.getDeclaredMethods();
        boolean hasSecretDecl = false, hasObjectDecl = false;
        for (Method m : declared) {
            if (m.getName().equals("secret")) hasSecretDecl = true;
            if (m.getDeclaringClass() == Object.class) hasObjectDecl = true;
        }
        check(hasSecretDecl, "getDeclaredMethods includes private");
        check(!hasObjectDecl, "getDeclaredMethods excludes inherited");

        checkThrows(NoSuchMethodException.class,
                () -> Sample.class.getMethod("nonexistent"), "getMethod missing NSME");
        checkThrows(NoSuchMethodException.class,
                () -> Sample.class.getMethod("secret", int.class),
                "getMethod private NSME");
        check(Sample.class.getDeclaredMethod("secret", int.class) != null,
                "getDeclaredMethod finds private");
        checkThrows(NullPointerException.class,
                () -> Sample.class.getMethod(null, int.class), "getMethod null name NPE");
        // null parameterTypes is treated as an empty array -> looks up add() -> NSME
        checkThrows(NoSuchMethodException.class,
                () -> Sample.class.getMethod("add", (Class<?>[]) null),
                "getMethod null paramTypes treated as empty");
    }

    public static void main(String[] args) throws Exception {
        testMetadata();
        testGenericSignature();
        testExceptionTypes();
        testInvoke();
        testInvocationTargetException();
        testAccessibility();
        testVarArgs();
        testBridgeSynthetic();
        testDefaultMethod();
        testAnnotationDefaultValue();
        testAnnotations();
        testEqualsHashCodeToString();
        testDiscovery();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
