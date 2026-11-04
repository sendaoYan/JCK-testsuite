/*
 * @test
 * @summary java.beans.Expression conformance: ctor/accessor identity, null-args -> empty,
 *          defensive cloning of arguments, arity/overload dispatch (most-specific,
 *          ambiguity on null), value caching (stale cache, setValue binding incl. null,
 *          execute() recompute), static methods via Class target, instance methods of
 *          Class.class, "new" constructor/array/Character forms, Class.forName special
 *          case, array "get", null-argument matching, no int->long widening, exact
 *          exception matrix (NPE/NoSuchMethodException/ISE unwrapped/IAE), toString forms.
 * @run main ExpressionTest
 */

import java.beans.Expression;
import java.beans.Statement;

public class ExpressionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }
    static final Object[] NOARGS = new Object[0];

    // ---- sample beans (public static nested classes: dispatch is via public methods) ----
    public static class Bean {
        public int calls;
        public String text = "t0";
        public int pub;
        public Bean() {}
        public void hit() { calls++; }
        public String getText() { return text; }
        public void setText(String s) { text = s; }
        public void takeLong(long v) { calls = (int) v; }
        public String takeNull(String s) { return "got:" + s; }
        public String pick(String s) { return "str:" + s; }
        public String pick(Object o) { return "obj:" + o; }
        public void setWiden(Object o) { calls = 77; }
    }
    public static class Fixed { public int v = 7; public Fixed() {} }
    public static class PkgCtor { PkgCtor() {} }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Expression");
        check(Statement.class.isAssignableFrom(c), "extends Statement");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) names.add(m.getName());
        }
        for (String n : new String[]{"getValue", "setValue", "execute"}) {
            check(names.contains(n), "declares public " + n);
        }
    }

    static void testAccessorsAndCtor() throws Exception {
        Expression e = new Expression("hello", "length", new Object[0]);
        check(Integer.valueOf(5).equals(e.getValue()), "value");
        check("hello".equals(e.getTarget()), "target");
        check("length".equals(e.getMethodName()), "method name");

        Expression up = new Expression("hello", "toUpperCase", NOARGS);
        check("hello".equals(up.getTarget()), "target");
        check("toUpperCase".equals(up.getMethodName()), "methodName");
        check("HELLO".equals(up.getValue()), "getValue computes");

        StringBuilder sb = new StringBuilder();
        Expression e2 = new Expression(sb, "toString", NOARGS);
        check(e2.getTarget() == sb, "getTarget identity");
        check("toString".equals(e2.getMethodName()), "getMethodName");
        Object[] args = new Object[]{"a"};
        Expression e3 = new Expression("xy", "concat", args);
        args[0] = "b";
        check("a".equals(e3.getArguments()[0]), "ctor clones arguments array");
        check(e3.getArguments().length == 1, "arguments length");
        check(e3.getArguments()[0] == args[0] || "a".equals(e3.getArguments()[0]), "arguments element");
        Object[] g1 = e3.getArguments();
        Object[] g2 = e3.getArguments();
        check(g1 != g2, "getArguments returns fresh clone per call");
        g1[0] = "zzz";
        check("a".equals(e3.getArguments()[0]), "mutating getArguments result has no effect");
        Expression e4 = new Expression("abc", "length", null);
        check(e4.getArguments().length == 0, "null arguments normalized to empty array");
        check(Integer.valueOf(3).equals(e4.getValue()), "null args evaluates like empty args");
        Expression e5 = new Expression("abc", "length", NOARGS);
        check(e5.getArguments().length == 0, "empty arguments stay empty");
        // ctor itself performs no validation: nulls are only rejected at evaluation time
        Expression e6 = new Expression(null, null, null);
        check(e6.getTarget() == null, "ctor accepts null target");
        check(e6.getMethodName() == null, "ctor accepts null methodName");
        check(e6.getArguments().length == 0, "ctor accepts null arguments");
        Object marker = new Object();
        Expression pre = new Expression(marker, "hello", "toUpperCase", NOARGS);
        check(marker == pre.getValue(), "pre-set value returned as-is");
        check(marker == pre.getValue(), "pre-set value stable");
    }

    static void testDispatchByArityAndSpecificity() throws Exception {
        check("llo".equals(new Expression("hello", "substring", new Object[]{Integer.valueOf(2)}).getValue()),
                "arity 1 picks substring(int)");
        check("ll".equals(new Expression("hello", "substring",
                new Object[]{Integer.valueOf(2), Integer.valueOf(4)}).getValue()),
                "arity 2 picks substring(int,int)");
        check("hellohel".equals(new Expression("hello", "concat", new Object[]{"hel"}).getValue()),
                "single-arg method dispatch");
        Bean b = new Bean();
        check("str:s".equals(new Expression(b, "pick", new Object[]{"s"}).getValue()),
                "most specific overload wins for String arg vs Object overload");
        check("obj:5".equals(new Expression(b, "pick", new Object[]{Integer.valueOf(5)}).getValue()),
                "Object overload used when String overload does not apply");
        final Bean fb = new Bean();
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression(fb, "pick", new Object[]{null}).getValue(); }
        }, "null arg with String/Object overloads is ambiguous");
        check("got:null".equals(new Expression(new Bean(), "takeNull", new Object[]{null}).getValue()),
                "null arg matches any single method signature");
    }

    static void testCachingSemantics() throws Exception {
        Bean b = new Bean();
        b.setText("one");
        Expression e = new Expression(b, "getText", NOARGS);
        check("one".equals(e.getValue()), "first getValue evaluates");
        b.setText("two");
        check("one".equals(e.getValue()), "second getValue returns cached value");
        e.setValue("manual");
        check("manual".equals(e.getValue()), "setValue overrides cache");
        b.setText("three");
        check("manual".equals(e.getValue()), "setValue result survives target mutation");
        Expression n = new Expression("x", "length", NOARGS);
        n.setValue(null);
        check(n.getValue() == null, "null value is bound");
        check(n.getValue() == null, "null value stays bound (no re-evaluation)");
        Bean c = new Bean();
        Expression ex = new Expression(c, "hit", NOARGS);
        ex.execute();
        check(ex.getValue() == null, "void method yields null value");
        check(c.calls == 1, "execute evaluates exactly once");
        ex.getValue();
        check(c.calls == 1, "getValue after execute does not re-evaluate");
        Expression pre = new Expression("PRE", "x", "toUpperCase", NOARGS);
        pre.execute();
        check("X".equals(pre.getValue()), "execute() re-evaluates even over a pre-set value");
        check("X".equals(pre.getValue()), "value after execute cached");
    }

    static void testStaticAndClassTargets() throws Exception {
        check(Integer.valueOf(9).equals(
                new Expression(Math.class, "max", new Object[]{Integer.valueOf(3), Integer.valueOf(9)}).getValue()),
                "static method via Class target");
        check(Integer.valueOf(5).equals(
                new Expression(Math.class, "abs", new Object[]{Integer.valueOf(-5)}).getValue()),
                "Integer arg matches int parameter (boxing)");
        check(Integer.valueOf(42).equals(
                new Expression(Integer.class, "parseInt", new Object[]{"42"}).getValue()),
                "static Integer.parseInt with String arg");
        check(Object.class == (Class<?>) new Expression(String.class, "getSuperclass", NOARGS).getValue(),
                "instance method of Class.class reachable with Class target");
        check(String.class == (Class<?>) new Expression(Class.class, "forName", new Object[]{"java.lang.String"}).getValue(),
                "Class.forName special case");
        // JDK-8: multi-arg forName still resolves using args[0] (no arity guard)
        check(String.class == (Class<?>) new Expression(Class.class, "forName",
                new Object[]{"java.lang.String", Boolean.TRUE}).getValue(),
                "forName with extra args resolves via first arg (JDK-8 behavior)");
    }

    static void testNewForms() throws Exception {
        Object sb = new Expression(StringBuilder.class, "new", new Object[]{"abc"}).getValue();
        check(sb instanceof StringBuilder, "\"new\" invokes constructor");
        check("abc".equals(sb.toString()), "constructor argument passed through");
        check(sb.getClass() == StringBuilder.class, "constructed exact type");
        Object arr = new Expression(String[].class, "new", new Object[]{"a", "b"}).getValue();
        check(arr instanceof String[], "\"new\" on array Class creates array");
        check(java.lang.reflect.Array.getLength(arr) == 2, "nary array constructor length from arg count");
        check("b".equals(java.lang.reflect.Array.get(arr, 1)), "array element from argument");
        Object iarr = new Expression(int[].class, "new",
                new Object[]{Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)}).getValue();
        check(iarr instanceof int[], "primitive array construction");
        check(java.lang.reflect.Array.getLength(iarr) == 3, "primitive array length");
        check(Integer.valueOf(2).equals(java.lang.reflect.Array.get(iarr, 1)), "boxed element stored in primitive array");
        check(Character.valueOf('x').equals(
                new Expression(Character.class, "new", new Object[]{"xyz"}).getValue()),
                "Character String-ctor special case uses first char");
        check(Integer.valueOf(123).equals(
                new Expression(Integer.class, "new", new Object[]{"123"}).getValue()),
                "Integer(String) constructor via \"new\"");
        Object fixed = new Expression(Fixed.class, "new", NOARGS).getValue();
        check(fixed instanceof Fixed, "\"new\" with no args uses Class.newInstance fallback");
        check(((Fixed) fixed).v == 7, "no-arg constructed instance is initialized");
        checkThrows(java.lang.InstantiationException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression(Runnable.class, "new", NOARGS).getValue(); }
        }, "\"new\" on an interface");
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression(Fixed.class, "new", new Object[]{"x"}).getValue(); }
        }, "\"new\" with wrong arity");
        checkThrows(java.lang.IllegalAccessException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression(PkgCtor.class, "new", NOARGS).getValue(); }
        }, "\"new\" no-arg fallback on package-private constructor");
    }

    static void testArrayAccess() throws Exception {
        String[] sa = {"a", "b"};
        check("b".equals(new Expression(sa, "get", new Object[]{Integer.valueOf(1)}).getValue()),
                "array \"get\" loads element");
        int[] ia = {7};
        check(Integer.valueOf(7).equals(new Expression(ia, "get", new Object[]{Integer.valueOf(0)}).getValue()),
                "array \"get\" boxes primitive element");
        boolean[] ba = {true};
        check(Boolean.TRUE.equals(new Expression(ba, "get", new Object[]{Integer.valueOf(0)}).getValue()),
                "array \"get\" on boolean[]");
        final String[] oob = {"a"};
        checkThrows(java.lang.ArrayIndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression(oob, "get", new Object[]{Integer.valueOf(5)}).getValue(); }
        }, "array \"get\" out of bounds");
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression(new String[]{"a"}, "length", NOARGS).getValue(); }
        }, "array \"length\" is not a supported method name");
    }

    static void testBoxingAndWidening() throws Exception {
        final Bean b = new Bean();
        check(new Expression(b, "takeLong", new Object[]{Long.valueOf(6)}).getValue() == null
                && b.calls == 6, "Long arg matches long parameter");
        final Bean b2 = new Bean();
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression(b2, "takeLong", new Object[]{Integer.valueOf(5)}).getValue(); }
        }, "no int->long widening: Integer arg does not match long parameter");
        final Bean b3 = new Bean();
        checkThrows(java.lang.IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression(b3, "takeLong", new Object[]{null}).getValue(); }
        }, "null argument cannot be unboxed to primitive parameter");
        Bean b4 = new Bean();
        check(new Expression(b4, "setWiden", new Object[]{Integer.valueOf(1)}).getValue() == null
                && b4.calls == 77, "Integer arg matches Object parameter");
    }

    public static class Thrower {
        final RuntimeException token = new IllegalStateException("boom-ise");
        public void go() { throw token; }
    }

    static void testExceptionMatrix() throws Exception {
        Thrower t = new Thrower();
        try {
            new Expression(t, "go", NOARGS).getValue();
            check(false, "exception from invoked method must propagate");
        } catch (IllegalStateException ise) {
            check(ise == t.token, "exception from invoked method propagates unwrapped, same instance");
        }
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression("x", "noSuchMethodAtAll", NOARGS).getValue(); }
        }, "unknown method");
        checkThrows(java.lang.NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression(null, "length", NOARGS).getValue(); }
        }, "null target fails at evaluation");
        checkThrows(java.lang.NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression("x", null, NOARGS).getValue(); }
        }, "null methodName fails at evaluation");
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Expression("x", "class", NOARGS).getValue(); }
        }, "\"class\" is not a supported method name");
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                new Expression("x", "substring", new Object[]{"notnum"}).getValue();
            }
        }, "right arity but wrong argument type: no matching overload");
    }

    static void testToString() throws Exception {
        Expression e = new Expression("hello", "length", NOARGS);
        String s = e.toString();
        check(s.startsWith("<unbound>="), "unbound value rendered as <unbound>");
        check(s.contains("\"hello\".length()"), "String target quoted, method name and empty args");
        e.getValue();
        check(e.toString().contains("Integer="), "bound value rendered by type name");
        check(e.toString().contains(".length()"), "statement part unchanged after evaluation");
        Expression v = new Expression("hello", "toUpperCase", NOARGS);
        v.setValue("V");
        check(v.toString().startsWith("\"V\"="), "String value rendered quoted");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testAccessorsAndCtor();
        testDispatchByArityAndSpecificity();
        testCachingSemantics();
        testStaticAndClassTargets();
        testNewForms();
        testArrayAccess();
        testBoxingAndWidening();
        testExceptionMatrix();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
