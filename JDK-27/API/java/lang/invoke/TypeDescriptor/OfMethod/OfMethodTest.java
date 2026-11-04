/*
 * @test
 * @summary java.lang.invoke.TypeDescriptor.OfMethod conformance: white-box + black-box behavior, transformation algebra, index negatives and a user implementation.
 * @run main OfMethodTest
 */

import java.lang.invoke.MethodType;
import java.lang.invoke.TypeDescriptor;
import java.lang.invoke.TypeDescriptor.OfField;
import java.lang.invoke.TypeDescriptor.OfMethod;
import java.util.List;

public class OfMethodTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.invoke.TypeDescriptor$OfMethod");
        check(c.isInterface(), "is an interface");
        check(c.getInterfaces().length == 1
              && c.getInterfaces()[0] == TypeDescriptor.class, "extends TypeDescriptor");
        for (String m : new String[]{"parameterCount", "parameterType", "returnType",
                "parameterArray", "parameterList", "changeReturnType", "changeParameterType",
                "dropParameterTypes", "insertParameterTypes"}) {
            boolean found = false;
            for (java.lang.reflect.Method mm : c.getDeclaredMethods())
                if (mm.getName().equals(m)) found = true;
            check(found, "declares " + m);
        }
        check(OfMethod.class.isAssignableFrom(MethodType.class),
              "MethodType implements OfMethod");
        check(OfMethod.class.isAssignableFrom(
                      Class.forName("java.lang.constant.MethodTypeDesc")),
              "MethodTypeDesc implements OfMethod");
        check(!OfMethod.class.isAssignableFrom(Class.class), "Class is not OfMethod");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // MethodType through its OfMethod view
        @SuppressWarnings("unchecked")
        OfMethod<Class<?>, MethodType> mt =
                (OfMethod<Class<?>, MethodType>) (Object) MethodType.methodType(
                        String.class, int.class, boolean.class);
        check(mt.parameterCount() == 2, "parameterCount");
        check(mt.parameterType(0) == int.class, "parameterType(0)");
        check(mt.returnType() == String.class, "returnType");
        check(mt.parameterList().equals(List.of(int.class, boolean.class)), "parameterList");
        check(mt.parameterArray().length == 2, "parameterArray");
        check(mt.descriptorString().equals("(IZ)Ljava/lang/String;"), "descriptorString");

        // transformations return new OfMethod views
        check(mt.changeReturnType(int.class).descriptorString().equals("(IZ)I"),
              "changeReturnType");
        check(mt.changeParameterType(0, long.class).descriptorString().equals("(JZ)Ljava/lang/String;"),
              "changeParameterType");
        check(mt.dropParameterTypes(0, 1).descriptorString().equals("(Z)Ljava/lang/String;"),
              "dropParameterTypes");
        check(mt.insertParameterTypes(0, Object.class).descriptorString()
                      .equals("(Ljava/lang/Object;IZ)Ljava/lang/String;"),
              "insertParameterTypes");

        // MethodTypeDesc behaves identically through the same interface
        java.lang.constant.MethodTypeDesc d =
                java.lang.constant.MethodTypeDesc.ofDescriptor("(IZ)Ljava/lang/String;");
        @SuppressWarnings("unchecked")
        OfMethod<?, ?> dv = (OfMethod<?, ?>) d;
        check(dv.parameterCount() == 2, "desc parameterCount");
        check(dv.descriptorString().equals("(IZ)Ljava/lang/String;"), "desc descriptor");
        @SuppressWarnings("unchecked")
        OfMethod<java.lang.constant.ClassDesc, ?> typed =
                (OfMethod<java.lang.constant.ClassDesc, ?>) (Object) d;
        check(typed.changeReturnType(java.lang.constant.ConstantDescs.CD_int)
                      .descriptorString().equals("(IZ)I"),
              "desc changeReturnType");

        // the views compose: describeConstable of MethodType is an OfMethod too
        java.util.Optional<java.lang.constant.MethodTypeDesc> nd =
                MethodType.methodType(void.class, long.class).describeConstable();
        check(nd.isPresent() && OfMethod.class.isAssignableFrom(nd.get().getClass()),
              "nominal form is an OfMethod");
    }

    // ---- helpers ----

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
            String frag, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                if (t.getMessage() != null && t.getMessage().contains(frag)) {
                    passed++; return;
                }
                throw new AssertionError(msg + ": expected message fragment \"" + frag
                        + "\" but got \"" + t.getMessage() + "\"");
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- transformation algebra through the OfMethod view ----
    static void testTransformations() {
        @SuppressWarnings("unchecked")
        OfMethod<Class<?>, MethodType> mt = (OfMethod<Class<?>, MethodType>) (Object)
                MethodType.methodType(String.class, int.class, boolean.class);
        check(mt.insertParameterTypes(0, Object.class, String.class).descriptorString()
                      .equals("(Ljava/lang/Object;Ljava/lang/String;IZ)Ljava/lang/String;"),
                "insertParameterTypes with several types");
        check(mt.insertParameterTypes(2, long.class).descriptorString()
                      .equals("(IZJ)Ljava/lang/String;"),
                "insertParameterTypes at the tail");
        check(mt.dropParameterTypes(0, 2).descriptorString().equals("()Ljava/lang/String;"),
                "dropParameterTypes removes all parameters");
        check(mt.dropParameterTypes(1, 1).descriptorString().equals("(IZ)Ljava/lang/String;"),
                "empty drop is an identity shape");
        check(mt.changeParameterType(1, char.class).descriptorString()
                      .equals("(IC)Ljava/lang/String;"), "changeParameterType(1, char)");
        check(mt.changeReturnType(void.class).descriptorString().equals("(IZ)V"),
                "changeReturnType to void");
        // parameterArray is a snapshot: mutating it cannot alias the method type
        Class<?>[] arr = mt.parameterArray();
        arr[0] = long.class;
        check(mt.parameterType(0) == int.class, "parameterArray is defensive");
        check(mt.parameterList().size() == 2, "parameterList size");
        check(mt.parameterList().get(1) == boolean.class, "parameterList element");
        check(mt.parameterList().equals(java.util.Arrays.asList(int.class, boolean.class)),
                "parameterList content");
    }

    // ---- index and null negatives ----
    static void testNegatives() {
        @SuppressWarnings("unchecked")
        OfMethod<Class<?>, MethodType> mt = (OfMethod<Class<?>, MethodType>) (Object)
                MethodType.methodType(String.class, int.class, boolean.class);
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> mt.parameterType(9),
                "parameterType out of range");
        checkThrows(ArrayIndexOutOfBoundsException.class, () -> mt.parameterType(-1),
                "negative parameterType index");
        checkThrows(ArrayIndexOutOfBoundsException.class,
                () -> mt.changeParameterType(9, long.class),
                "changeParameterType out of range");
        checkThrowsMsg(IndexOutOfBoundsException.class,
                () -> mt.dropParameterTypes(2, 1), "start=2 end=1", "reversed drop range");
        checkThrowsMsg(IndexOutOfBoundsException.class,
                () -> mt.dropParameterTypes(0, 9), "start=0 end=9", "drop end beyond arity");
        checkThrowsMsg(IndexOutOfBoundsException.class,
                () -> mt.insertParameterTypes(9, Object.class), "bad index: 9",
                "insert position beyond arity");
        checkThrows(NullPointerException.class, () -> mt.changeReturnType(null),
                "null return type -> NPE");
        checkThrows(NullPointerException.class, () -> mt.changeParameterType(0, null),
                "null parameter type -> NPE");
    }

    // ---- a user implementation honoring the full generic contract ----
    static final class TD implements OfField<TD> {
        private final String d;
        TD(String d) { this.d = d; }
        @Override public String descriptorString() { return d; }
        @Override public boolean isArray() { return d.startsWith("["); }
        @Override public boolean isPrimitive() { return d.length() == 1; }
        @Override public TD componentType() { return isArray() ? new TD(d.substring(1)) : null; }
        @Override public TD arrayType() { return new TD("[" + d); }
    }

    static final class MD implements OfMethod<TD, MD> {
        private final TD ret;
        private final TD[] ps;
        MD(TD ret, TD[] ps) { this.ret = ret; this.ps = ps; }
        @Override public int parameterCount() { return ps.length; }
        @Override public TD parameterType(int i) { return ps[i]; }
        @Override public TD returnType() { return ret; }
        @Override public TD[] parameterArray() { return ps.clone(); }
        @Override public List<TD> parameterList() { return java.util.Arrays.asList(ps); }
        @Override public String descriptorString() {
            StringBuilder sb = new StringBuilder("(");
            for (TD p : ps) sb.append(p.descriptorString());
            sb.append(")").append(ret.descriptorString());
            return sb.toString();
        }
        @Override public MD changeReturnType(TD r) { return new MD(r, ps.clone()); }
        @Override public MD changeParameterType(int i, TD p) {
            TD[] c = ps.clone(); c[i] = p; return new MD(ret, c);
        }
        @Override public MD dropParameterTypes(int start, int end) {
            TD[] c = new TD[ps.length - (end - start)];
            int k = 0;
            for (int i = 0; i < ps.length; i++) if (i < start || i >= end) c[k++] = ps[i];
            return new MD(ret, c);
        }
        @Override public MD insertParameterTypes(int pos, @SuppressWarnings("unchecked") TD... ins) {
            TD[] c = new TD[ps.length + ins.length];
            System.arraycopy(ps, 0, c, 0, pos);
            System.arraycopy(ins, 0, c, pos, ins.length);
            System.arraycopy(ps, pos, c, pos + ins.length, ps.length - pos);
            return new MD(ret, c);
        }
    }

    static void testUserImplementation() {
        TD i = new TD("I");
        TD s = new TD("Ljava/lang/String;");
        MD m = new MD(s, new TD[] { i, s });
        check(m.descriptorString().equals("(ILjava/lang/String;)Ljava/lang/String;"),
                "user impl descriptor");
        check(m.parameterCount() == 2, "user impl parameterCount");
        check(m.parameterType(0) == i && m.returnType() == s, "user impl accessors");
        check(m.parameterList().size() == 2, "user impl parameterList");
        check(m.changeReturnType(i).descriptorString().equals("(ILjava/lang/String;)I"),
                "user impl changeReturnType");
        check(m.changeParameterType(0, s).descriptorString()
                      .equals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
                "user impl changeParameterType");
        check(m.dropParameterTypes(0, 1).descriptorString()
                      .equals("(Ljava/lang/String;)Ljava/lang/String;"),
                "user impl dropParameterTypes");
        check(m.insertParameterTypes(0, s).descriptorString()
                      .equals("(Ljava/lang/String;ILjava/lang/String;)Ljava/lang/String;"),
                "user impl insertParameterTypes");
        check(m.parameterArray().length == 2, "user impl parameterArray");
    }

    // ---- MethodTypeDesc parity through the interface ----
    static void testDescParity() {
        java.lang.constant.MethodTypeDesc d =
                java.lang.constant.MethodTypeDesc.ofDescriptor("(IZ)Ljava/lang/String;");
        @SuppressWarnings("unchecked")
        OfMethod<?, ?> dv = (OfMethod<?, ?>) d;
        check(dv.parameterType(0).descriptorString().equals("I"),
                "desc parameterType descriptor");
        check(dv.returnType().descriptorString().equals("Ljava/lang/String;"),
                "desc returnType descriptor");
        check(dv.parameterList().size() == 2, "desc parameterList size");
        check(dv.parameterArray().length == 2, "desc parameterArray length");
        check(dv.descriptorString().equals("(IZ)Ljava/lang/String;"),
                "desc descriptor stability");
        @SuppressWarnings("unchecked")
        OfMethod<java.lang.constant.ClassDesc, ?> typed =
                (OfMethod<java.lang.constant.ClassDesc, ?>) (Object) d;
        check(typed.changeParameterType(1, java.lang.constant.ConstantDescs.CD_long)
                      .descriptorString().equals("(IJ)Ljava/lang/String;"),
                "desc changeParameterType");
        check(typed.insertParameterTypes(0, java.lang.constant.ConstantDescs.CD_Object)
                      .descriptorString()
                      .equals("(Ljava/lang/Object;IZ)Ljava/lang/String;"),
                "desc insertParameterTypes");
        check(dv.dropParameterTypes(1, 2).descriptorString().equals("(I)Ljava/lang/String;"),
                "desc dropParameterTypes");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testTransformations();
        testNegatives();
        testUserImplementation();
        testDescParity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
