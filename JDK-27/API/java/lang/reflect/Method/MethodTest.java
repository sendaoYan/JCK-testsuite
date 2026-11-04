/*
 * @test
 * @summary java.lang.reflect.Method conformance: white-box structure + black-box behavior.
 * @run main MethodTest
 */

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures (nested: nestmate-accessible for positive paths) ----
    static class Sample {
        public int add(int a, int b) {
            return a + b;
        }
    }

    static class Biz {
        public int add(int a, int b) { return a + b; }
        public static String greet(String name) { return "hello " + name; }
        public <T> T identity(T value) { return value; }
        public void fail() { throw new IllegalStateException("boom-marker"); }
        public int pick(int x) { return 1; }
        public String pick(String s) { return "pick:" + s; }
        public void variadic(int first, String... rest) { }
        public int sum(int... xs) {
            int t = 0;
            for (int x : xs) t += x;
            return t;
        }
    }

    static class StringCmp implements Comparable<StringCmp> {
        public int compareTo(StringCmp o) { return 42; }
    }

    interface DefaultIface {
        default String hi() { return "default-hi"; }
    }

    @interface Marked {
        String value() default "dflt";
    }

    // ---- Part 1: white-box (structural, retained from previous revision) ----
    static void testWhiteBox() throws Exception {
        Method m = Sample.class.getMethod("add", int.class, int.class);
        check("add".equals(m.getName()), "getName");
        check(m.getReturnType() == int.class, "getReturnType");
        check(m.getGenericReturnType() == int.class, "getGenericReturnType");
        check(m.getParameterTypes().length == 2, "getParameterTypes");
        check(m.getParameterCount() == 2, "getParameterCount");
        check(m.getDeclaringClass() == Sample.class, "getDeclaringClass");
        check(!Modifier.isStatic(m.getModifiers()), "instance method");
        Sample s = new Sample();
        check(m.invoke(s, 2, 3).equals(5), "invoke");
        check(Sample.class.getMethods().length >= 1, "getMethods");
        check(Sample.class.getDeclaredMethods().length >= 1, "getDeclaredMethods");
        check(m.toString() != null, "toString");
        check(m.toGenericString() != null, "toGenericString");
        check(Modifier.isFinal(Method.class.getModifiers()), "Method is final");
        check(Method.class.getSuperclass().getName().equals("java.lang.reflect.Executable"),
              "Method extends Executable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Biz biz = new Biz();

        // 1. exact-signature lookup + invoke round-trip
        Method add = Biz.class.getDeclaredMethod("add", int.class, int.class);
        check(((Integer) add.invoke(biz, 2, 3)) == 5, "add(2,3) == 5");
        check(((Integer) add.invoke(biz, -4, 4)) == 0, "add(-4,4) == 0");
        check(add.invoke(biz, 10, 20) instanceof Integer, "int return boxed to Integer");
        check(add.getModifiers() == Modifier.PUBLIC, "add is public");
        check(!add.isVarArgs() && !add.isBridge() && !add.isDefault() && !add.isSynthetic(),
              "add plain flags");
        check(add.getExceptionTypes().length == 0, "add declares no exceptions");

        // 2. static method: null receiver and ignored receiver
        Method greet = Biz.class.getDeclaredMethod("greet", String.class);
        check("hello jck".equals(greet.invoke(null, "jck")), "static invoke null receiver");
        check("hello x".equals(greet.invoke("receiver-ignored", "x")), "static invoke ignores receiver");
        check(greet.getModifiers() == (Modifier.PUBLIC | Modifier.STATIC), "greet public static");

        // 3. generic method
        Method identity = Biz.class.getDeclaredMethod("identity", Object.class);
        check("gen".equals(identity.invoke(biz, "gen")), "identity round-trip String");
        check(Integer.valueOf(7).equals(identity.invoke(biz, 7)), "identity round-trip Integer");
        check(identity.getTypeParameters().length == 1 && "T".equals(identity.getTypeParameters()[0].getName()),
              "identity type parameter T");
        check(identity.getGenericReturnType().getTypeName().equals("T"), "generic return type name T");
        check(identity.getAnnotatedReturnType().getType() instanceof java.lang.reflect.TypeVariable
              && "T".equals(identity.getAnnotatedReturnType().getType().getTypeName()),
              "annotated return keeps type variable");

        // 4. exception wrapping
        Method fail = Biz.class.getDeclaredMethod("fail");
        try {
            fail.invoke(biz);
            check(false, "fail() must wrap");
        } catch (InvocationTargetException e) {
            Throwable c = e.getCause();
            check(c instanceof IllegalStateException, "wrapped cause exact type");
            check("boom-marker".equals(c.getMessage()), "wrapped cause message");
            check(e.getTargetException() == c, "getTargetException == getCause");
        }

        // 5. overload resolution by exact parameter types
        Method pickInt = Biz.class.getDeclaredMethod("pick", int.class);
        Method pickStr = Biz.class.getDeclaredMethod("pick", String.class);
        check(((Integer) pickInt.invoke(biz, 5)) == 1, "pick(int) overload");
        check("pick:s".equals(pickStr.invoke(biz, "s")), "pick(String) overload");
        check(!pickInt.equals(pickStr), "overloads not equal");

        // 6. varargs (reflective invoke requires the packed array form)
        Method sum = Biz.class.getDeclaredMethod("sum", int[].class);
        check(sum.isVarArgs(), "sum isVarArgs");
        check(((Integer) sum.invoke(biz, new Object[]{new int[]{1, 2, 3}})) == 6, "varargs packed invoke");
        check(((Integer) sum.invoke(biz, new Object[]{new int[0]})) == 0, "varargs empty invoke");
        check(((Integer) sum.invoke(biz, new Object[]{new int[]{4, 5}})) == 9, "varargs multi invoke");
        Method variadic = Biz.class.getDeclaredMethod("variadic", int.class, String[].class);
        check(variadic.isVarArgs() && variadic.getParameterTypes()[1] == String[].class,
              "variadic trailing array parameter");
        variadic.invoke(biz, 1, new String[]{"a", "b"});
        try { sum.invoke(biz, 1, 2, 3); check(false, "spread varargs must fail"); }
        catch (IllegalArgumentException e) { check(true, "IllegalArgumentException spread varargs"); }

        // 7. private method in another nest: access control
        Method secret = MethodSeparate.class.getDeclaredMethod("secret", String.class);
        check(Modifier.isPrivate(secret.getModifiers()), "secret is private");
        try {
            secret.invoke(new MethodSeparate(), "x");
            check(false, "private invoke must be denied");
        } catch (IllegalAccessException e) {
            check(true, "IllegalAccessException before setAccessible");
        }
        secret.setAccessible(true);
        check("s3cr3t:ok".equals(secret.invoke(new MethodSeparate(), "ok")), "private invoke after setAccessible");

        // 8. invoke argument/receiver mismatches
        try { add.invoke(biz, 1); check(false, "arity mismatch must fail"); }
        catch (IllegalArgumentException e) { check(true, "IllegalArgumentException wrong arity"); }
        try { add.invoke(biz, "a", "b"); check(false, "type mismatch must fail"); }
        catch (IllegalArgumentException e) { check(true, "IllegalArgumentException wrong type"); }
        try { add.invoke("not-a-Biz", 1, 2); check(false, "receiver mismatch must fail"); }
        catch (IllegalArgumentException e) { check(true, "IllegalArgumentException wrong receiver"); }
        try { add.invoke(null, 1, 2); check(false, "null receiver must fail"); }
        catch (NullPointerException e) { check(true, "NullPointerException null receiver"); }

        // 9. bridge/synthetic on Comparable implementation
        Method plain = null, bridge = null;
        for (Method m : StringCmp.class.getDeclaredMethods()) {
            if (m.getParameterTypes()[0] == StringCmp.class) plain = m;
            if (m.getParameterTypes()[0] == Object.class) bridge = m;
        }
        check(plain != null && !plain.isBridge() && !plain.isSynthetic(), "compareTo(StringCmp) plain");
        check(bridge != null && bridge.isBridge() && bridge.isSynthetic(), "compareTo(Object) bridge+synthetic");
        check(((Integer) bridge.invoke(new StringCmp(), new StringCmp())) == 42, "bridge invoke dispatches");

        // 10. default methods and annotation defaults
        Method hi = DefaultIface.class.getMethod("hi");
        check(hi.isDefault(), "hi isDefault");
        check(!add.isDefault(), "class method not default");
        Method value = Marked.class.getMethod("value");
        check("dflt".equals(value.getDefaultValue()), "getDefaultValue present");
        check(add.getDefaultValue() == null, "getDefaultValue null for plain method");

        // 11. equality/identity semantics and string forms
        Method add2 = Biz.class.getDeclaredMethod("add", int.class, int.class);
        check(add.equals(add2) && add2.equals(add), "Method equals by value");
        check(add.hashCode() == add2.hashCode(), "Method hashCode consistent");
        check(!add.equals(greet), "different methods not equal");
        check(add.toString().contains("add(int,int)") || add.toString().contains("add(int, int)"),
              "toString contains signature");
        check(identity.toGenericString().contains("<T>"), "toGenericString shows type parameter");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

// second top-level class = a different nest: its private members are not
// accessible to MethodTest, exercising the access-control paths.
class MethodSeparate {
    private String secret(String in) { return "s3cr3t:" + in; }
}
