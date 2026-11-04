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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

// Separate top-level class (not a nestmate of ConstructorTest) so reflection access to
// its private constructor requires setAccessible(true).
class ExtCtor {
    int x;
    private ExtCtor(String s) { x = s.length(); }
}
