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
 *          Deepen batch: exact-arity varargs contract (spread form is rejected),
 *          receiver/null-argument IAE message fragments, RuntimeException cause
 *          identity, denied-access message fragment, overload resolution, nested
 *          generic return type decomposition, interface static/default/abstract
 *          dispatch, bridge virtual dispatch, documented hashCode formula,
 *          annotation default kinds (Class/int[]/enum/String[]) and the
 *          AnnotatedType family on methods.
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
import java.lang.reflect.ParameterizedType;
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

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
                               String fragment, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof AssertionError) throw (AssertionError) t;
            if (!expected.isInstance(t)) {
                throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                        + " but got " + t.getClass().getName(), t);
            }
            String m = t.getMessage();
            if (m == null || !m.contains(fragment)) {
                throw new AssertionError(msg + ": message <" + m
                        + "> does not contain <" + fragment + ">");
            }
            passed++;
            return;
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

    // ---- fixtures added by the deepen batch ----

    static class Rich {
        public String concat(String a, String b) { return a + b; }
        public static String statConcat(String p) { return "[" + p + "]"; }
        public java.util.Map<String, java.util.List<Integer>> combine() { return null; }
        public <T extends Number> T pick(T t) { return t; }
    }

    static class RtSample {
        public static final IllegalStateException BOOM = new IllegalStateException("rt-boom");
        public int throwRt() { throw BOOM; }
    }

    static class Overload {
        public String m(int x) { return "int:" + x; }
        public String m(int x, int y) { return "sum:" + (x + y); }
        public String m(String s) { return "str:" + s; }
    }

    static class EchoSub extends StringImpl {
        public String echo(String s) { return "sub:" + s; }
    }

    interface IStatic {
        static String stat() { return "stat-v"; }
    }

    interface IGreet {
        default String dflt() { return "dflt-v"; }
        String abs();
    }

    static class GreetImpl implements IGreet {
        static int count;
        public String abs() { count++; return "abs-v"; }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface RichAnn {
        Class<?> cls() default Object.class;
        int[] nums() default {2, 4, 6};
        RetentionPolicy pol() default RetentionPolicy.RUNTIME;
        String[] names() default {"a", "b"};
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
        // Use a separate top-level class (not a nestmate): JDK-11+ permits reflection
        // access to a nestmate's private member without setAccessible, so the IAE path
        // must target a non-nest class to remain a valid contract assertion.
        ExtMethod em = new ExtMethod();
        Method secret = ExtMethod.class.getDeclaredMethod("secret", int.class);
        check(Modifier.isPrivate(secret.getModifiers()), "secret private");
        checkThrows(IllegalAccessException.class, () -> secret.invoke(em, 4),
                "private invoke without accessible IAE");
        secret.setAccessible(true);
        check(secret.invoke(em, 4).equals(8), "private invoke after setAccessible");
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

    // ---- tests added by the deepen batch ----

    static void testInvokeExactValues() throws Exception {
        Rich r = new Rich();
        Method concat = Rich.class.getMethod("concat", String.class, String.class);
        check("abcd".equals(concat.invoke(r, "ab", "cd")), "concat exact value");
        Method statConcat = Rich.class.getMethod("statConcat", String.class);
        check("[x]".equals(statConcat.invoke(null, "x")), "static concat null receiver");
        check("[y]".equals(statConcat.invoke(r, "y")), "static concat receiver ignored");
    }

    static void testInvokeVarargsExactArity() throws Exception {
        // Method.invoke performs no varargs packing: the argument count must equal
        // the declared parameter count and the last argument must be the array.
        Sample s = new Sample();
        Method sum = Sample.class.getMethod("sum", int[].class);
        Object a = sum.invoke(s, new Object[]{new int[]{5, 6, 7}});
        Object b = sum.invoke(s, (Object) new int[]{5, 6, 7});
        check(a.equals(18) && b.equals(18), "varargs two array forms equal 18");
        check(sum.invoke(s, new Object[]{new int[0]}).equals(0), "empty varargs array -> 0");
        check(sum.invoke(s, new Object[]{new int[]{9}}).equals(9), "single element varargs");
        checkThrowsMsg(IllegalArgumentException.class, () -> sum.invoke(s, 5, 6, 7),
                "wrong number of arguments", "varargs spread form is rejected");
        checkThrowsMsg(IllegalArgumentException.class, () -> sum.invoke(s),
                "wrong number of arguments", "varargs zero args rejected");
    }

    static void testInvokeReceiverAndNullArg() throws Exception {
        Sample s = new Sample();
        Method add = Sample.class.getMethod("add", int.class, int.class);
        checkThrowsMsg(IllegalArgumentException.class, () -> add.invoke("not-a-Sample", 1, 2),
                "is not an instance of", "wrong receiver type IAE message");
        checkThrows(IllegalArgumentException.class, () -> add.invoke(s, null, 1),
                "null for primitive parameter IAE");
        checkThrowsMsg(IllegalArgumentException.class, () -> add.invoke(s, 1L, 2),
                "argument type mismatch", "Long is not narrowed to int");
    }

    static void testInvokeExceptionIdentity() throws Exception {
        RtSample r = new RtSample();
        Method rt = RtSample.class.getMethod("throwRt");
        try {
            rt.invoke(r);
            throw new AssertionError("expected InvocationTargetException");
        } catch (InvocationTargetException ite) {
            passed++;
            check(ite.getCause() == RtSample.BOOM, "RuntimeException cause is the thrown object");
            check(ite.getTargetException() == ite.getCause(),
                    "getTargetException == getCause same object");
            check(ite.getCause().getClass() == IllegalStateException.class, "cause exact class");
            check("rt-boom".equals(ite.getCause().getMessage()), "cause message preserved");
        }
        // a null varargs array is passed through verbatim -> NPE inside -> ITE
        Sample s = new Sample();
        Method sum = Sample.class.getMethod("sum", int[].class);
        try {
            sum.invoke(s, new Object[]{null});
            throw new AssertionError("expected InvocationTargetException");
        } catch (InvocationTargetException ite) {
            passed++;
            check(ite.getCause().getClass() == NullPointerException.class,
                    "null varargs array -> ITE wrapping NPE");
        }
    }

    static void testAccessMessage() throws Exception {
        // Nestmates (JDK-11+) make a nested private member accessible, so the
        // denied-access path must target the non-nest top-level ExtMethod class.
        ExtMethod em = new ExtMethod();
        Method secret = ExtMethod.class.getDeclaredMethod("secret", int.class);
        try {
            secret.invoke(em, 4);
            throw new AssertionError("expected IllegalAccessException");
        } catch (IllegalAccessException e) {
            passed++;
            check(e.getMessage().contains("access a member of class"),
                    "denied message fragment");
            check(e.getMessage().contains("ExtMethod"), "denied message names member class");
        }
        secret.setAccessible(true);
        check(secret.invoke(em, 5).equals(10), "private invoke after setAccessible");
    }

    static void testOverloads() throws Exception {
        Overload o = new Overload();
        Method m1 = Overload.class.getMethod("m", int.class);
        Method m2 = Overload.class.getMethod("m", int.class, int.class);
        Method m3 = Overload.class.getMethod("m", String.class);
        check("int:7".equals(m1.invoke(o, 7)), "overload (int) selected");
        check("sum:3".equals(m2.invoke(o, 1, 2)), "overload (int,int) selected");
        check("str:z".equals(m3.invoke(o, "z")), "overload (String) selected");
        check(!m1.equals(m2) && !m2.equals(m3) && !m1.equals(m3),
                "overloads pairwise unequal");
        check(m1.getParameterCount() == 1 && m2.getParameterCount() == 2
                && m3.getParameterCount() == 1, "parameter counts distinguish overloads");
    }

    static void testGenericReturnTypeDeep() throws Exception {
        Method combine = Rich.class.getMethod("combine");
        Type grt = combine.getGenericReturnType();
        check(grt instanceof ParameterizedType, "Map return is ParameterizedType");
        ParameterizedType pt = (ParameterizedType) grt;
        check(pt.getRawType() == java.util.Map.class, "raw type Map");
        check(pt.getActualTypeArguments().length == 2, "two actual type arguments");
        check(pt.getActualTypeArguments()[0] == String.class, "first argument String");
        Type second = pt.getActualTypeArguments()[1];
        check(second instanceof ParameterizedType, "second argument nested PT");
        ParameterizedType inner = (ParameterizedType) second;
        check(inner.getRawType() == java.util.List.class, "nested raw List");
        check(inner.getActualTypeArguments()[0] == Integer.class, "nested argument Integer");
        check(pt.getTypeName().equals(
                "java.util.Map<java.lang.String, java.util.List<java.lang.Integer>>"),
                "full generic type name");

        Method pick = Rich.class.getMethod("pick", Number.class);
        check(pick.getGenericReturnType() instanceof TypeVariable, "pick return TypeVariable");
        TypeVariable<?> tv = (TypeVariable<?>) pick.getGenericReturnType();
        check("T".equals(tv.getName()), "type variable name T");
        check(tv.getBounds().length == 1 && tv.getBounds()[0] == Number.class,
                "type variable bound Number");
        check(pick.getGenericParameterTypes()[0] instanceof TypeVariable,
                "pick parameter TypeVariable");
        check(pick.getAnnotatedReturnType().getType() instanceof TypeVariable,
                "annotated return keeps the type variable");
        // JDK-9+ toGenericString prints type-parameter bounds (JDK-8 printed "<T>").
        check(pick.toGenericString().contains("<T extends java.lang.Number>"),
                "toGenericString shows bound");
    }

    static void testInterfaceInvoke() throws Exception {
        Method stat = IStatic.class.getMethod("stat");
        check(Modifier.isStatic(stat.getModifiers()), "interface static method flag");
        check("stat-v".equals(stat.invoke(null)), "interface static invoke null receiver");

        GreetImpl g = new GreetImpl();
        Method d = IGreet.class.getMethod("dflt");
        check(d.isDefault(), "default method flag");
        check("dflt-v".equals(d.invoke(g)), "default body runs via invoke");

        Method abs = IGreet.class.getMethod("abs");
        check(Modifier.isAbstract(abs.getModifiers()), "abstract interface method flag");
        int before = GreetImpl.count;
        check("abs-v".equals(abs.invoke(g)), "abstract dispatches to implementation");
        check(GreetImpl.count == before + 1, "virtual dispatch ran the impl body");
        check(abs.getDeclaringClass() == IGreet.class,
                "abstract method declaring class is the interface");
    }

    static void testBridgeDispatch() throws Exception {
        Method bridge = null;
        for (Method m : EchoSub.class.getMethods()) {
            if (m.getName().equals("echo") && m.isBridge()) bridge = m;
        }
        check(bridge != null, "bridge method found");
        check("sub:q".equals(bridge.invoke(new EchoSub(), (Object) "q")),
                "bridge invoke dispatches virtually");
        check(bridge.getDeclaringClass() == EchoSub.class,
                "each overriding subclass carries its own bridge");
        check(bridge.hashCode() == EchoSub.class.getMethod("echo", String.class).hashCode(),
                "bridge and real method share the name-based hash");
        check(bridge.getGenericReturnType() == Object.class,
                "bridge erased return type Object");
    }

    static void testDefaultValueKinds() throws Exception {
        check(RichAnn.class.getMethod("cls").getDefaultValue() == Object.class,
                "Class-valued default");
        int[] nums = (int[]) RichAnn.class.getMethod("nums").getDefaultValue();
        check(nums.length == 3 && nums[0] == 2 && nums[1] == 4 && nums[2] == 6,
                "int[] default contents");
        check(RichAnn.class.getMethod("pol").getDefaultValue() == RetentionPolicy.RUNTIME,
                "enum default");
        String[] names = (String[]) RichAnn.class.getMethod("names").getDefaultValue();
        check(names.length == 2 && "a".equals(names[0]) && "b".equals(names[1]),
                "String[] default contents");
    }

    static void testStringFormsAndHashFormula() throws Exception {
        Method add = Sample.class.getMethod("add", int.class, int.class);
        check(add.toString().contains("public int "), "toString modifier and return");
        check(add.toString().contains(".add(int,int)"), "toString signature");
        check(add.toGenericString().contains("public int "),
                "toGenericString modifier and return");
        check(add.hashCode() == (Sample.class.getName().hashCode() ^ "add".hashCode()),
                "documented hashCode formula");
        Method viaDeclared = Sample.class.getDeclaredMethod("add", int.class, int.class);
        check(add.equals(viaDeclared) && add.hashCode() == viaDeclared.hashCode(),
                "getMethod equals getDeclaredMethod member");
        try {
            Sample.class.getMethod("nonexistent");
            throw new AssertionError("expected NoSuchMethodException");
        } catch (NoSuchMethodException e) {
            passed++;
            check(e.getMessage().contains("nonexistent()"), "NSME message names the method");
        }
    }

    static void testAnnotatedTypeFamily() throws Exception {
        Method add = Sample.class.getMethod("add", int.class, int.class);
        check(add.getAnnotatedReturnType().getType() == int.class,
                "annotated return type for primitive");
        check(add.getAnnotatedParameterTypes().length == 2, "annotated parameters length");
        check(add.getAnnotatedParameterTypes()[0].getType() == int.class,
                "annotated parameter type");
        check(add.getAnnotatedExceptionTypes().length == 0, "no declared exceptions");
        check(add.getAnnotatedReceiverType() != null,
                "instance method has a receiver type");
        Method stat = Sample.class.getMethod("stat", int.class);
        check(stat.getAnnotatedReceiverType() == null,
                "static method receiver type is null");
        Method thr = Sample.class.getMethod("thrower");
        check(thr.getAnnotatedExceptionTypes().length == 1
                && thr.getAnnotatedExceptionTypes()[0].getType() == IOException.class,
                "annotated exception type");
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
        testInvokeExactValues();
        testInvokeVarargsExactArity();
        testInvokeReceiverAndNullArg();
        testInvokeExceptionIdentity();
        testAccessMessage();
        testOverloads();
        testGenericReturnTypeDeep();
        testInterfaceInvoke();
        testBridgeDispatch();
        testDefaultValueKinds();
        testStringFormsAndHashFormula();
        testAnnotatedTypeFamily();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

// Separate top-level class (not a nestmate of MethodTest) so reflection access to its
// private method requires setAccessible(true).
class ExtMethod {
    private int secret(int x) { return x * 2; }
}
