/*
 * @test
 * @summary java.beans.Statement conformance: ctor/accessor identity, null-args -> empty,
 *          defensive cloning, execute() instance/static/Class-target dispatch, "new"
 *          constructor form, array "set"/"get" element store with boxing and exact
 *          failure matrix (AIOOBE/CCE/IAE), no property-name or field-name fallback,
 *          exceptions from invoked method propagate unwrapped, execute() idempotence
 *          for pure calls, repeated execution of side effects, toString forms.
 * @run main StatementTest
 */

import java.beans.Statement;
import java.util.ArrayList;
import java.util.List;

public class StatementTest {
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

    public static class Bean {
        public int calls;
        public String text = "t0";
        public int pub;
        public Bean() {}
        public void hit() { calls++; }
        public String getText() { return text; }
        public void setText(String s) { text = s; }
        public void takeLong(long v) { calls = (int) v; }
    }
    public static class StatHolder {
        public static String mark = "init";
        public static void setMark(String m) { mark = m; }
    }
    public static class Fixed { public int v = 3; public Fixed() {} }
    public static class Thrower {
        final RuntimeException token = new IllegalStateException("boom-ise");
        public void go() { throw token; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Statement");
        check(!c.isInterface(), "is a class");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public class");
        check(java.beans.Expression.class.isAssignableFrom(c) == false,
                "Statement is the base class, not an Expression");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) names.add(m.getName());
        }
        for (String n : new String[]{"execute", "getTarget", "getMethodName", "getArguments"}) {
            check(names.contains(n), "declares public " + n);
        }
    }

    static void testAccessorsAndCtor() throws Exception {
        Statement s = new Statement("target", "toString", new Object[0]);
        check("target".equals(s.getTarget()), "target");
        check("toString".equals(s.getMethodName()), "method name");
        check(s.getArguments().length == 0, "arguments");

        List<String> list = new ArrayList<String>();
        Statement st = new Statement(list, "add", new Object[]{"x"});
        check(list == st.getTarget(), "target");
        check("add".equals(st.getMethodName()), "methodName");
        check(st.getArguments() != null && st.getArguments().length == 1, "arguments");

        Object[] args = new Object[]{"a"};
        Statement s2 = new Statement("hello", "concat", args);
        args[0] = "b";
        check("a".equals(s2.getArguments()[0]), "ctor clones arguments array");
        Object[] g1 = s2.getArguments();
        Object[] g2 = s2.getArguments();
        check(g1 != g2, "getArguments returns fresh clone per call");
        g1[0] = "zzz";
        check("a".equals(s2.getArguments()[0]), "mutating getArguments result has no effect");
        Statement s3 = new Statement("x", "toString", null);
        check(s3.getArguments().length == 0, "null arguments normalized to empty array");
        Statement s4 = new Statement(null, null, null);
        check(s4.getTarget() == null, "ctor accepts null target (no eager validation)");
        check(s4.getMethodName() == null, "ctor accepts null methodName");
        check(s4.getArguments().length == 0, "ctor accepts null arguments");
        String shared = new String("same");
        Statement s5 = new Statement("h", "concat", new Object[]{shared});
        check(s5.getArguments()[0] == shared, "argument elements stored by reference");
        check(s5.getArguments()[0].equals("same"), "argument element value");
    }

    static void testExecuteInstanceSideEffects() throws Exception {
        List<String> list = new ArrayList<String>();
        Statement add = new Statement(list, "add", new Object[]{"x"});
        add.execute();
        check(list.contains("x"), "execute applies side effect");
        Statement s2 = new Statement(list, "clear", new Object[0]);
        s2.execute();
        check(list.isEmpty(), "execute clear");

        StringBuilder sb = new StringBuilder("a");
        new Statement(sb, "append", new Object[]{"b"}).execute();
        new Statement(sb, "append", new Object[]{Integer.valueOf(1)}).execute();
        check("ab1".equals(sb.toString()), "execute chains append(String) and append(int) overloads");

        Bean b = new Bean();
        new Statement(b, "setText", new Object[]{"via-statement"}).execute();
        check("via-statement".equals(b.text), "setter method invoked with argument");

        Bean counter = new Bean();
        Statement hit = new Statement(counter, "hit", NOARGS);
        hit.execute();
        hit.execute();
        check(counter.calls == 2, "execute() twice runs the method twice");

        Statement pure = new Statement("hello", "toString", NOARGS);
        pure.execute();
        pure.execute();
        check(true, "execute() of pure call is repeatable");

        List<String> two = new ArrayList<String>();
        two.add("x");
        two.add("y");
        new Statement(two, "remove", new Object[]{Integer.valueOf(0)}).execute();
        check(two.size() == 1, "interface method remove(int) executed");
        check("y".equals(two.get(0)), "Integer arg matched int parameter, removed right element");
    }

    static void testExecuteStaticAndClassTargets() throws Exception {
        Statement stat = new Statement(StatHolder.class, "setMark", new Object[]{"set-by-statement"});
        stat.execute();
        check("set-by-statement".equals(StatHolder.mark), "static method invoked via Class target");
        StatHolder.mark = "init";

        new Statement(Math.class, "max", new Object[]{Integer.valueOf(1), Integer.valueOf(2)}).execute();
        check(true, "static Math.max executes (result discarded)");
        new Statement(String.class, "getSuperclass", NOARGS).execute();
        check(true, "instance method of Class.class executes via Class target");

        Statement ctor = new Statement(Fixed.class, "new", NOARGS);
        ctor.execute();
        check(true, "\"new\" constructor form executes through Statement");
    }

    static void testArrayOperations() throws Exception {
        String[] sa = {"a", "b"};
        new Statement(sa, "set", new Object[]{Integer.valueOf(0), "z"}).execute();
        check("z".equals(sa[0]), "array \"set\" stores element");
        check("b".equals(sa[1]), "other elements untouched");

        int[] ia = {1};
        new Statement(ia, "set", new Object[]{Integer.valueOf(0), Integer.valueOf(42)}).execute();
        check(ia[0] == 42, "array \"set\" unboxes Integer into int[]");

        boolean[] ba = {false};
        new Statement(ba, "set", new Object[]{Integer.valueOf(0), Boolean.TRUE}).execute();
        check(ba[0], "array \"set\" on boolean[]");

        new Statement(sa, "get", new Object[]{Integer.valueOf(1)}).execute();
        check(true, "array \"get\" executes (result discarded)");

        final String[] oob = {"a"};
        checkThrows(java.lang.ArrayIndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                new Statement(oob, "set", new Object[]{Integer.valueOf(5), "z"}).execute();
            }
        }, "array \"set\" out of bounds");
        checkThrows(java.lang.ArrayIndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                new Statement(oob, "get", new Object[]{Integer.valueOf(-1)}).execute();
            }
        }, "array \"get\" negative index");
        checkThrows(java.lang.ClassCastException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                new Statement(oob, "set", new Object[]{"zero", "z"}).execute();
            }
        }, "array \"set\" with non-Integer index");
        checkThrows(java.lang.IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                new Statement(oob, "set", new Object[]{Integer.valueOf(0), Integer.valueOf(9)}).execute();
            }
        }, "array \"set\" with wrong element type");
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                new Statement(oob, "length", NOARGS).execute();
            }
        }, "array \"length\" is not a supported method name");
    }

    static void testExceptionMatrix() throws Exception {
        checkThrows(java.lang.NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Statement(null, "toString", NOARGS).execute(); }
        }, "null target fails at execute time");
        checkThrows(java.lang.NullPointerException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Statement("x", null, NOARGS).execute(); }
        }, "null methodName fails at execute time");
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Statement("x", "noSuchMethodAtAll", NOARGS).execute(); }
        }, "unknown method");
        final Bean b = new Bean();
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Statement(b, "text", new Object[]{"v"}).execute(); }
        }, "property-name style is not translated to setText");
        checkThrows(java.lang.NoSuchMethodException.class, new ThrowingRunnable() {
            public void run() throws Throwable { new Statement(b, "pub", new Object[]{Integer.valueOf(3)}).execute(); }
        }, "public field name is not a method (no field fallback)");
        Thrower t = new Thrower();
        try {
            new Statement(t, "go", NOARGS).execute();
            check(false, "exception from invoked method must propagate");
        } catch (IllegalStateException ise) {
            check(ise == t.token, "exception from invoked method propagates unwrapped, same instance");
        }
        final Bean b2 = new Bean();
        checkThrows(java.lang.IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                new Statement(b2, "takeLong", new Object[]{null}).execute();
            }
        }, "null argument cannot be unboxed to primitive parameter");
        checkThrows(java.lang.IllegalAccessException.class, new ThrowingRunnable() {
            public void run() throws Throwable {
                new Statement(PkgCtor.class, "new", NOARGS).execute();
            }
        }, "\"new\" no-arg fallback on package-private constructor");
    }
    public static class PkgCtor { PkgCtor() {} }

    static void testToString() throws Exception {
        Statement s = new Statement("hello", "substring",
                new Object[]{Integer.valueOf(1), Integer.valueOf(2)});
        String t = s.toString();
        check(t.contains("\"hello\""), "String target quoted in toString");
        check(t.contains("substring"), "method name present");
        check(t.contains("Integer, Integer"), "argument types rendered by class name");
        check(t.endsWith(");"), "Java-style closing");
        Statement e = new Statement("x", "toString", NOARGS);
        check(e.toString().contains("\"x\".toString()"), "empty argument list rendered as ()");
        Statement n = new Statement(null, "m", new Object[]{null});
        check(n.toString().contains("null.m(null)"), "null target and null arg rendered as null");
        Statement b = new Statement(new Bean(), "hit", NOARGS);
        check(b.toString().contains(".hit()"), "nested-class target rendered by unqualified name");
        Statement strArg = new Statement("h", "concat", new Object[]{"el"});
        check(strArg.toString().contains("\"el\""), "String argument quoted in toString");
        boolean hasEquals = false;
        for (java.lang.reflect.Method m : Statement.class.getDeclaredMethods()) {
            if (m.getName().equals("equals")) hasEquals = true;
        }
        check(!hasEquals, "Statement does not override equals (identity semantics)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testAccessorsAndCtor();
        testExecuteInstanceSideEffects();
        testExecuteStaticAndClassTargets();
        testArrayOperations();
        testExceptionMatrix();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
