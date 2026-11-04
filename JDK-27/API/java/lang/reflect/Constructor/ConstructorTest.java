/*
 * @test
 * @summary java.lang.reflect.Constructor conformance: full contract coverage.
 *          parameter/exception type reflection incl. generic type parameters,
 *          newInstance success + wrong arity/type -> IllegalArgumentException,
 *          private without setAccessible -> IllegalAccessException, abstract/interface
 *          -> InstantiationException, InvocationTargetException wraps the exact cause,
 *          isVarArgs/isSynthetic, getAnnotation/getDeclaredAnnotations/
 *          getParameterAnnotations/getAnnotatedReturnType/getAnnotatedReceiverType,
 *          equals/hashCode/toString/toGenericString, getConstructors vs
 *          getDeclaredConstructors, and NoSuchMethodException/NPE discovery paths.
 *          Deepen batch: exact-type newInstance round-trip, "argument type mismatch"
 *          / "wrong number of arguments" message fragments, the exact-arity varargs
 *          contract (spread form rejected), enum reflective creation denied with
 *          "Cannot reflectively create enum objects", generic-throwing constructor
 *          (TypeVariable exception), receiver-type evidence per class kind,
 *          documented hashCode formula, NSME "<init>" message and interface
 *          constructor absence.
 * @run main ConstructorTest
 */

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;

public class ConstructorTest {
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
    @Target(ElementType.CONSTRUCTOR)
    @interface CAnn { String value(); }

    static class Sample {
        public int x;
        public Sample() { x = 0; }
        public Sample(int v) { x = v; }
        private Sample(String s) { x = s.length(); }
        public Sample(int... vs) { for (int v : vs) x += v; }
        @CAnn("c") public Sample(long l, String s) { x = (int) l; }
        public <T> Sample(T t, boolean b) { x = b ? 1 : 0; }
        public Sample(boolean throwIt) throws IOException {
            if (throwIt) throw new IOException("ctor boom");
            x = -1;
        }
    }

    static abstract class AbstractThing {
        public AbstractThing() { }
    }

    // Non-static inner class: its constructor has a receiver (the enclosing instance),
    // so getAnnotatedReceiverType() returns a non-null value.
    class InnerCtor {
        public InnerCtor(long l, String s) { }
    }

    interface Iface { }

    // ---- fixtures added by the deepen batch ----

    static class Pair {
        public final int a;
        public final String b;
        public Pair(int a, String b) { this.a = a; this.b = b; }
    }

    static class GenCtor {
        public <E extends Exception> GenCtor(E e) throws E { }
    }

    enum Color { RED, GREEN }

    static class VaTarget {
        public int sum;
        public VaTarget(int... vs) { for (int v : vs) sum += v; }
    }

    static void testMetadata() throws Exception {
        Constructor<Sample> c = Sample.class.getConstructor(int.class);
        check(c.getParameterTypes().length == 1, "getParameterTypes length");
        check(c.getParameterTypes()[0] == int.class, "getParameterTypes element");
        check(c.getParameterCount() == 1, "getParameterCount");
        check(c.getModifiers() == Modifier.PUBLIC, "public modifier");
        check(c.getName().equals(Sample.class.getName()), "getName == class name");
        check(c.getDeclaringClass() == Sample.class, "getDeclaringClass");
        check(c.getGenericParameterTypes().length == 1, "getGenericParameterTypes length");
    }

    static void testGenericConstructor() throws Exception {
        Constructor<Sample> c = Sample.class.getConstructor(Object.class, boolean.class);
        TypeVariable<?>[] tp = c.getTypeParameters();
        check(tp.length == 1, "generic constructor one type parameter");
        check(tp[0].getName().equals("T"), "type parameter name");
        check(c.getGenericParameterTypes()[0] instanceof TypeVariable,
                "generic parameter TypeVariable");
        check(c.getParameterTypes()[0] == Object.class, "erasure parameter Object");
    }

    static void testNewInstance() throws Exception {
        Constructor<Sample> c0 = Sample.class.getConstructor();
        Sample s0 = c0.newInstance();
        check(s0.x == 0, "newInstance no-arg");

        Constructor<Sample> ci = Sample.class.getConstructor(int.class);
        Sample s1 = ci.newInstance(42);
        check(s1.x == 42, "newInstance int");
        check(ci.newInstance(Integer.valueOf(-3)).x == -3, "newInstance boxed widening");

        Constructor<Sample> cl = Sample.class.getConstructor(long.class, String.class);
        check(cl.newInstance(9L, "ignored").x == 9, "newInstance two args");

        // wrong arity -> IllegalArgumentException
        checkThrows(IllegalArgumentException.class, () -> ci.newInstance(),
                "newInstance too few IAE");
        checkThrows(IllegalArgumentException.class, () -> ci.newInstance(1, 2),
                "newInstance too many IAE");
        checkThrows(IllegalArgumentException.class, () -> ci.newInstance((Object[]) null),
                "newInstance null args IAE");
        // wrong type -> IllegalArgumentException
        checkThrows(IllegalArgumentException.class, () -> ci.newInstance(1L),
                "newInstance wrong type IAE");
        checkThrows(IllegalArgumentException.class, () -> ci.newInstance("s"),
                "newInstance wrong type String IAE");
    }

    static void testAccessibility() throws Exception {
        // Use a separate top-level class (not a nestmate): JDK-11+ permits reflection
        // access to a nestmate's private member without setAccessible, so the IAE path
        // must target a non-nest class to remain a valid contract assertion.
        Constructor<ExtCtor> priv = ExtCtor.class.getDeclaredConstructor(String.class);
        check(Modifier.isPrivate(priv.getModifiers()), "private ctor");
        checkThrows(IllegalAccessException.class, () -> priv.newInstance("ab"),
                "private newInstance without accessible IAE");
        priv.setAccessible(true);
        ExtCtor s = priv.newInstance("abcd");
        check(s.x == 4, "private newInstance after setAccessible");
    }

    static void testInstantiationException() throws Exception {
        Constructor<AbstractThing> ac = AbstractThing.class.getDeclaredConstructor();
        checkThrows(InstantiationException.class, () -> ac.newInstance(),
                "abstract class InstantiationException");
    }

    static void testInvocationTargetException() throws Exception {
        Constructor<Sample> tc = Sample.class.getConstructor(boolean.class);
        try {
            tc.newInstance(true);
            throw new AssertionError("expected InvocationTargetException");
        } catch (InvocationTargetException ite) {
            passed++;
            check(ite.getCause() instanceof IOException, "ctor ITE cause IOException");
            check(ite.getTargetException() instanceof IOException, "ctor ITE target");
            check("ctor boom".equals(ite.getTargetException().getMessage()),
                    "ctor ITE message");
        }
        // non-throwing path works
        check(tc.newInstance(false).x == -1, "ctor false path works");
    }

    static void testVarArgs() throws Exception {
        Constructor<Sample> var = Sample.class.getConstructor(int[].class);
        check(var.isVarArgs(), "varargs constructor isVarArgs");
        check(var.newInstance((Object) new int[]{1, 2, 3}).x == 6,
                "varargs newInstance");
        Constructor<Sample> ci = Sample.class.getConstructor(int.class);
        check(!ci.isVarArgs(), "non-varargs not varargs");
    }

    static void testAnnotations() throws Exception {
        Constructor<Sample> ca = Sample.class.getConstructor(long.class, String.class);
        CAnn ann = ca.getAnnotation(CAnn.class);
        check(ann != null && "c".equals(ann.value()), "getAnnotation");
        Annotation[] declared = ca.getDeclaredAnnotations();
        boolean found = false;
        for (Annotation a : declared) {
            if (a instanceof CAnn) found = true;
        }
        check(found, "getDeclaredAnnotations includes CAnn");
        check(ca.getParameterAnnotations().length == 2, "getParameterAnnotations length");
        check(ca.getAnnotatedReturnType() != null, "getAnnotatedReturnType non-null");
        // JDK-27: a static nested class constructor has no receiver, so the annotated
        // receiver type is null (JDK-8 returned a non-null degenerate value here).
        check(ca.getAnnotatedReceiverType() == null,
                "getAnnotatedReceiverType null for static nested class");

        Constructor<?> ic = InnerCtor.class.getConstructor(ConstructorTest.class, long.class, String.class);
        java.lang.reflect.AnnotatedType ict = ic.getAnnotatedReceiverType();
        check(ict != null, "inner class getAnnotatedReceiverType non-null");
        check(ict.getType() == ConstructorTest.class,
                "inner class receiver type is enclosing class");

        check(Sample.class.getConstructor(int.class).getAnnotation(CAnn.class) == null,
                "unannotated getAnnotation null");
    }

    static void testEqualsHashCodeToString() throws Exception {
        Constructor<Sample> a = Sample.class.getConstructor(int.class);
        Constructor<Sample> b = Sample.class.getConstructor(int.class);
        Constructor<Sample> c = Sample.class.getConstructor();
        check(a.equals(b), "equals same ctor");
        check(a.hashCode() == b.hashCode(), "hashCode same");
        check(!a.equals(c), "equals different ctor");
        check(!a.equals(null), "equals null");
        check(!a.equals("x"), "equals non-Constructor");
        check(a.toString().contains(Sample.class.getName()), "toString contains class");
        check(a.toGenericString().contains("int"), "toGenericString contains type");
    }

    static void testDiscovery() throws Exception {
        Constructor<?>[] pub = Sample.class.getConstructors();
        // public constructors: (), (int), (int[]), (long,String), (Object,boolean), (boolean)
        check(pub.length == 6, "getConstructors length " + pub.length);
        for (Constructor<?> c : pub) {
            check(Modifier.isPublic(c.getModifiers()), "getConstructors all public");
        }

        Constructor<?>[] all = Sample.class.getDeclaredConstructors();
        boolean hasPrivate = false;
        for (Constructor<?> c : all) {
            if (Modifier.isPrivate(c.getModifiers())) hasPrivate = true;
        }
        check(hasPrivate, "getDeclaredConstructors includes private");
        check(all.length == 7, "getDeclaredConstructors length " + all.length);

        checkThrows(NoSuchMethodException.class,
                () -> Sample.class.getConstructor(String.class), "getConstructor private NSME");
        checkThrows(NoSuchMethodException.class,
                () -> Sample.class.getConstructor(float.class), "getConstructor missing NSME");
        check(Sample.class.getDeclaredConstructor(String.class) != null,
                "getDeclaredConstructor finds private");
        // null parameterTypes is treated as empty -> no-arg constructor
        check(Sample.class.getConstructor((Class<?>[]) null).getParameterCount() == 0,
                "getConstructor null paramTypes -> no-arg");
        check(Sample.class.getDeclaredConstructor((Class<?>[]) null).getParameterCount() == 0,
                "getDeclaredConstructor null paramTypes -> no-arg");
        // a null element inside parameterTypes -> NoSuchMethodException (no matching ctor)
        checkThrows(NoSuchMethodException.class,
                () -> Sample.class.getConstructor(int.class, null),
                "getConstructor null element NSME");
        checkThrows(NoSuchMethodException.class,
                () -> Sample.class.getDeclaredConstructor(int.class, null),
                "getDeclaredConstructor null element NSME");
    }

    // ---- tests added by the deepen batch ----

    static void testNewInstanceExactType() throws Exception {
        Constructor<Pair> pc = Pair.class.getConstructor(int.class, String.class);
        Pair p = pc.newInstance(7, "seven");
        check(p.getClass() == Pair.class, "exact runtime type");
        check(p.a == 7 && "seven".equals(p.b), "field values round-trip");
        check(pc.newInstance(0, "").b.isEmpty(), "empty string round-trip");
    }

    static void testNewInstanceMismatchMessages() throws Exception {
        Constructor<Sample> ci = Sample.class.getConstructor(int.class);
        checkThrowsMsg(IllegalArgumentException.class, () -> ci.newInstance("x"),
                "argument type mismatch", "String for int parameter");
        checkThrows(IllegalArgumentException.class, () -> ci.newInstance(new Object[]{null}),
                "null for int parameter IAE");
        // Constructor.newInstance performs no varargs packing either: the argument
        // count must equal the declared parameter count.
        Constructor<VaTarget> vc = VaTarget.class.getConstructor(int[].class);
        check(vc.isVarArgs(), "VaTarget varargs constructor");
        checkThrowsMsg(IllegalArgumentException.class, () -> vc.newInstance(1, 2),
                "wrong number of arguments", "varargs spread form rejected for ctor");
        VaTarget r1 = vc.newInstance(new Object[]{new int[]{1, 2, 3}});
        VaTarget r2 = vc.newInstance((Object) new int[]{1, 2, 3});
        check(r1.sum == 6 && r2.sum == 6, "two array forms equal sum");
        check(vc.newInstance(new Object[]{new int[0]}).sum == 0, "empty varargs array");
    }

    static void testEnumNewInstance() throws Exception {
        Constructor<?>[] ctors = Color.class.getDeclaredConstructors();
        check(ctors.length == 1, "enum has a single declared ctor");
        Constructor<?> ec = ctors[0];
        check(ec.getParameterTypes().length == 2
                && ec.getParameterTypes()[0] == String.class
                && ec.getParameterTypes()[1] == int.class,
                "enum ctor erasure is (String,int)");
        check(!ec.isSynthetic(), "enum ctor not synthetic");
        ec.setAccessible(true);
        checkThrowsMsg(IllegalArgumentException.class,
                () -> ec.newInstance("X", Integer.valueOf(0)),
                "Cannot reflectively create enum objects",
                "reflective enum creation denied");
    }

    static void testGenericExceptionConstructor() throws Exception {
        Constructor<GenCtor> gc = GenCtor.class.getConstructor(Exception.class);
        TypeVariable<?>[] tvs = gc.getTypeParameters();
        check(tvs.length == 1 && "E".equals(tvs[0].getName()), "ctor type parameter E");
        check(tvs[0].getBounds()[0] == Exception.class, "E bound Exception");
        check(gc.getExceptionTypes().length == 1
                && gc.getExceptionTypes()[0] == Exception.class,
                "erased exception type Exception");
        check(gc.getGenericExceptionTypes()[0] instanceof TypeVariable,
                "generic exception type is a TypeVariable");
        check(gc.toGenericString().contains("GenCtor"),
                "toGenericString contains class name");
    }

    static void testReceiverTypeByKind() throws Exception {
        Constructor<?> ic = InnerCtor.class.getConstructor(ConstructorTest.class,
                long.class, String.class);
        java.lang.reflect.AnnotatedType r = ic.getAnnotatedReceiverType();
        check(r != null && r.getType() == ConstructorTest.class,
                "inner ctor receiver is the enclosing class");
        Constructor<Sample> sc = Sample.class.getConstructor(int.class);
        // JDK-9+ correctly returns null for a static-nested ctor; JDK-8 returned a
        // degenerate non-null value — locked version difference.
        check(sc.getAnnotatedReceiverType() == null,
                "JDK-9+ static-nested ctor receiver null");
    }

    static void testInterfaceConstructorAbsence() throws Exception {
        check(Iface.class.getConstructors().length == 0, "interface public ctors none");
        check(Iface.class.getDeclaredConstructors().length == 0,
                "interface declared ctors none");
        checkThrows(NoSuchMethodException.class, () -> Iface.class.getDeclaredConstructor(),
                "interface ctor lookup NSME");
    }

    static void testHashCodeFormulaAndLookup() throws Exception {
        Constructor<Sample> a = Sample.class.getConstructor(int.class);
        Constructor<Sample> b = Sample.class.getDeclaredConstructor(int.class);
        check(a.equals(b), "getConstructor equals getDeclaredConstructor member");
        check(a.hashCode() == b.hashCode(), "same hashCode");
        check(a.hashCode() == Sample.class.getName().hashCode(),
                "documented hashCode == declaring class name hash");
        try {
            Sample.class.getConstructor(float.class);
            throw new AssertionError("expected NoSuchMethodException");
        } catch (NoSuchMethodException e) {
            passed++;
            check(e.getMessage().contains("<init>"), "NSME message uses <init>");
            check(e.getMessage().contains("(float)"), "NSME message shows parameter types");
        }
    }

    static void testToStringForms() throws Exception {
        Constructor<Pair> pc = Pair.class.getConstructor(int.class, String.class);
        check(pc.toString().contains("public "), "toString visibility");
        check(pc.toString().contains("Pair(int,java.lang.String)"), "toString signature");
        check(pc.toGenericString().contains("Pair(int,java.lang.String)"),
                "toGenericString signature");
        Constructor<Sample> g = Sample.class.getConstructor(Object.class, boolean.class);
        check(g.toGenericString().contains("<T>"),
                "generic ctor toGenericString shows T");
    }

    public static void main(String[] args) throws Exception {
        testMetadata();
        testGenericConstructor();
        testNewInstance();
        testAccessibility();
        testInstantiationException();
        testInvocationTargetException();
        testVarArgs();
        testAnnotations();
        testEqualsHashCodeToString();
        testDiscovery();
        testNewInstanceExactType();
        testNewInstanceMismatchMessages();
        testEnumNewInstance();
        testGenericExceptionConstructor();
        testReceiverTypeByKind();
        testInterfaceConstructorAbsence();
        testHashCodeFormulaAndLookup();
        testToStringForms();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

// Separate top-level class (not a nestmate of ConstructorTest) so reflection access to
// its private constructor requires setAccessible(true).
class ExtCtor {
    int x;
    private ExtCtor(String s) { x = s.length(); }
}
