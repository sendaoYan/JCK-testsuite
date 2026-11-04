/*
 * @test
 * @summary java.beans.Beans conformance: white-box structure (all-static
 *          utility class, instantiate throws clauses), isInstanceOf /
 *          getInstanceOf matrices (exact class / superclass / interfaces /
 *          primitive / void / array probes, null semantics with exact
 *          exception types), design-time and GUI-available toggles
 *          (restored in finally), instantiate positive paths (nested class
 *          binary name, explicit loader, BeanInfo presence does not
 *          interfere, java.lang.String) and negative matrix (missing /
 *          empty / malformed dotted / null name, abstract class, private
 *          constructor, interface, array class - all with wrapped causes),
 *          BeanContext integration (bean added to context, null context
 *          accepted), Beans vs BeanContextSupport design-time flag
 *          independence, and JDK-27 specific signatures (4-arg
 *          AppletInitializer overload removed, setters declare no checked
 *          exceptions).
 * @run main BeansTest
 */

import java.beans.Beans;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextSupport;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class BeansTest {
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
    static Throwable thrown(ThrowingRunnable r) {
        try { r.run(); return null; }
        catch (Throwable t) { return t; }
    }

    // ---- sample beans used by instantiate() ----
    public static class SimpleBean {
        public boolean ctorRan;
        public SimpleBean() { ctorRan = true; }
    }
    public abstract static class AbstractBean {
        public AbstractBean() {}
    }
    public static class PrivateCtorBean {
        private PrivateCtorBean() {}
    }
    public static class InfoBean {
        public InfoBean() {}
    }
    public static class InfoBeanBeanInfo extends java.beans.SimpleBeanInfo {}

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.Beans");
        check(!c.isInterface(), "is a class");
    }

    static void testBlackBox() throws Exception {
        Beans.setDesignTime(true);
        check(Beans.isDesignTime(), "isDesignTime true");
        Beans.setDesignTime(false);
        check(!Beans.isDesignTime(), "isDesignTime false");

        Beans.setGuiAvailable(true);
        check(Beans.isGuiAvailable(), "isGuiAvailable true");
        Beans.setGuiAvailable(false);
        check(!Beans.isGuiAvailable(), "isGuiAvailable false");

        check(Beans.isInstanceOf("x", String.class), "isInstanceOf true");
        check(!Beans.isInstanceOf("x", Integer.class), "isInstanceOf false");
        check(Beans.isInstanceOf("x", CharSequence.class), "isInstanceOf interface");
        check("x".equals(Beans.getInstanceOf("x", String.class)), "getInstanceOf same");
        check("x".equals(Beans.getInstanceOf("x", CharSequence.class)), "getInstanceOf interface");

        Object o = Beans.instantiate(Thread.currentThread().getContextClassLoader(), "java.lang.String");
        check(o instanceof String, "instantiate by name");
        Object o2 = Beans.instantiate(Thread.currentThread().getContextClassLoader(), "java.lang.String", null);
        check(o2 instanceof String, "instantiate by name with BeanContext");
    }

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Beans.class;
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface()
                && !Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()),
                "Beans is a public concrete class");
        check(c.getSuperclass() == Object.class, "Beans extends Object directly");
        check(c.getInterfaces().length == 0, "Beans implements no interfaces");
        check(c.getTypeParameters().length == 0, "Beans is not generic");
        int staticCount = 0, total = 0;
        for (Method m : c.getDeclaredMethods()) { total++; if (Modifier.isStatic(m.getModifiers())) staticCount++; }
        check(total > 0 && staticCount == total, "all " + total + " declared methods are static");
        Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && ctors[0].getParameterTypes().length == 0
                && Modifier.isPublic(ctors[0].getModifiers()), "single public no-arg constructor");
        check(c.getDeclaredFields().length == 0, "no declared fields (no state, utility class)");
        check(c.getFields().length == 0, "no public constants");
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("instantiate")) {
                Class<?>[] ex = m.getExceptionTypes();
                boolean io = false, cnf = false;
                for (Class<?> e : ex) {
                    if (e == IOException.class) io = true;
                    if (e == ClassNotFoundException.class) cnf = true;
                }
                check(io, m + " declares IOException");
                check(cnf, m + " declares ClassNotFoundException");
            }
        }
    }

    static void testInstanceOfMatrix() {
        check(Beans.isInstanceOf("x", String.class), "isInstanceOf exact class");
        check(Beans.isInstanceOf("x", Object.class), "isInstanceOf superclass");
        check(Beans.isInstanceOf("x", CharSequence.class), "isInstanceOf interface");
        check(Beans.isInstanceOf("x", Comparable.class), "isInstanceOf second interface");
        check(Beans.isInstanceOf(Integer.valueOf(7), Number.class), "isInstanceOf Number superclass");
        check(Beans.isInstanceOf(Integer.valueOf(7), Comparable.class), "Integer implements Comparable");
        check(!Beans.isInstanceOf("x", Integer.class), "unrelated class false");
        check(!Beans.isInstanceOf(new Object(), String.class), "plain Object is not a String");
        check(Beans.isInstanceOf(new int[0], int[].class), "primitive array exact component");
        check(Beans.isInstanceOf(new int[0], Object.class), "primitive array is an Object");
        check(!Beans.isInstanceOf(new String[0], Object[].class),
                "array covariance NOT honored: String[] is not Object[] (isSubclass walk, probed)");
        check(!Beans.isInstanceOf(new String[0], CharSequence[].class),
                "String[] is not CharSequence[] (no covariance)");
        check(Beans.isInstanceOf(new String[0], Cloneable.class), "String[] implements Cloneable via interface walk");
        check(Beans.isInstanceOf(new int[0], Cloneable.class), "int[] implements Cloneable via interface walk");
        check(Beans.isInstanceOf(new String[0], java.io.Serializable.class),
                "String[] is Serializable via interface walk");
        check(!Beans.isInstanceOf("x", int[].class), "String is not int[]");
        check(!Beans.isInstanceOf("x", int.class), "primitive int target false");
        check(!Beans.isInstanceOf("x", long.class), "primitive long target false");
        check(!Beans.isInstanceOf("x", boolean.class), "primitive boolean target false");
        check(!Beans.isInstanceOf("x", void.class), "void target false");
        check(!Beans.isInstanceOf("x", null), "null target type false (no exception)");
        checkThrows(NullPointerException.class,
                () -> Beans.isInstanceOf(null, String.class), "null bean -> NPE");
        checkThrows(NullPointerException.class,
                () -> Beans.isInstanceOf(null, null), "null bean and null type -> NPE");
    }

    static void testGetInstanceOf() {
        String s = new String("bean");
        check(Beans.getInstanceOf(s, String.class) == s, "getInstanceOf identity for exact class");
        check(Beans.getInstanceOf(s, CharSequence.class) == s, "getInstanceOf identity for interface");
        check(Beans.getInstanceOf(s, Object.class) == s, "getInstanceOf identity for superclass");
        check(Beans.getInstanceOf(s, Integer.class) == s, "non-matching type returns target as-is");
        check(Beans.getInstanceOf(s, null) == s, "null target type returns target as-is");
        check(Beans.getInstanceOf(null, String.class) == null, "null bean returns null (no exception)");
        Object o = new Object();
        check(Beans.getInstanceOf(o, Runnable.class) == o, "non-matching interface returns target as-is");
        Integer n = Integer.valueOf(3);
        check(Beans.getInstanceOf(n, Number.class) == n, "Number view is identity for simple beans");
    }

    static void testDesignTimeToggle() {
        check(!Beans.isDesignTime(), "isDesignTime default false");
        try {
            Beans.setDesignTime(true);
            check(Beans.isDesignTime(), "setDesignTime(true) round-trip");
            Beans.setDesignTime(true);
            check(Beans.isDesignTime(), "setDesignTime(true) idempotent");
            Beans.setDesignTime(false);
            check(!Beans.isDesignTime(), "setDesignTime(false) round-trip");
        } finally {
            Beans.setDesignTime(false);
        }
        check(!Beans.isDesignTime(), "design-time restored to false in finally");
    }

    static void testGuiAvailableRelative() {
        boolean initial = Beans.isGuiAvailable();
        try {
            Beans.setGuiAvailable(false);
            check(!Beans.isGuiAvailable(), "setGuiAvailable(false) reflected by isGuiAvailable");
            Beans.setGuiAvailable(false);
            check(!Beans.isGuiAvailable(), "setGuiAvailable(false) idempotent");
        } finally {
            Beans.setGuiAvailable(initial);
        }
        check(Beans.isGuiAvailable() == initial, "gui-available restored to recorded initial value");
    }

    static void testInstantiatePositive() throws Exception {
        Object b1 = Beans.instantiate(null, "BeansTest$SimpleBean");
        check(b1 instanceof SimpleBean, "instantiate nested class by binary name");
        check(((SimpleBean) b1).ctorRan, "public no-arg constructor ran");
        Object b2 = Beans.instantiate(null, "BeansTest$SimpleBean");
        check(b2 instanceof SimpleBean && b2 != b1, "each instantiate call returns a fresh instance");
        Object b3 = Beans.instantiate(BeansTest.class.getClassLoader(), "BeansTest$SimpleBean");
        check(b3 instanceof SimpleBean && b3 != b1 && b3 != b2, "explicit classloader accepted");
        Object info = Beans.instantiate(null, "BeansTest$InfoBean");
        check(info instanceof InfoBean, "existing <Bean>BeanInfo class does not interfere with instantiate");
        Object str = Beans.instantiate(null, "java.lang.String");
        check(str instanceof String && ((String) str).isEmpty(), "java.lang.String via public no-arg ctor");
        Object str2 = Beans.instantiate(Thread.currentThread().getContextClassLoader(), "java.lang.String");
        check(str2 instanceof String && str2 != str, "context classloader yields another fresh String");
    }

    static void testInstantiateNegative() throws Exception {
        checkThrows(ClassNotFoundException.class,
                () -> Beans.instantiate(null, "no.such.Bean"), "missing class -> ClassNotFoundException");
        checkThrows(ClassNotFoundException.class,
                () -> Beans.instantiate(null, "com.example..Missing"), "malformed dotted name -> ClassNotFoundException");
        checkThrows(ClassNotFoundException.class,
                () -> Beans.instantiate(null, ""), "empty bean name -> ClassNotFoundException");
        checkThrows(NullPointerException.class,
                () -> Beans.instantiate(null, null), "null bean name -> NPE (undeclared)");
        Throwable t = thrown(() -> Beans.instantiate(null, "BeansTest$AbstractBean"));
        check(t instanceof ClassNotFoundException, "abstract class -> ClassNotFoundException (remapped)");
        check(t != null && t.getCause() instanceof InstantiationException,
                "abstract failure wraps InstantiationException");
        t = thrown(() -> Beans.instantiate(null, "BeansTest$PrivateCtorBean"));
        check(t instanceof ClassNotFoundException, "private ctor -> ClassNotFoundException (remapped)");
        check(t != null && t.getCause() instanceof IllegalAccessException,
                "private ctor failure wraps IllegalAccessException");
        t = thrown(() -> Beans.instantiate(null, "java.lang.Runnable"));
        check(t instanceof ClassNotFoundException, "interface name -> ClassNotFoundException (remapped)");
        check(t != null && t.getCause() instanceof InstantiationException,
                "interface failure wraps InstantiationException");
        t = thrown(() -> Beans.instantiate(null, "[Ljava.lang.String;"));
        check(t instanceof ClassNotFoundException, "array class name -> ClassNotFoundException (remapped)");
    }

    @SuppressWarnings("removal")
    static void testInstantiateWithBeanContext() throws Exception {
        BeanContextSupport ctx = new BeanContextSupport();
        Object b = Beans.instantiate(BeansTest.class.getClassLoader(), "BeansTest$SimpleBean", ctx);
        check(b instanceof SimpleBean, "3-arg instantiate returns the bean");
        check(ctx.contains(b), "bean was added to the BeanContext");
        check(ctx.size() == 1, "context holds exactly one member");
        Object b2 = Beans.instantiate(BeansTest.class.getClassLoader(), "BeansTest$SimpleBean", ctx);
        check(ctx.size() == 2 && ctx.contains(b2), "second bean added, context size 2");
        Object b3 = Beans.instantiate(BeansTest.class.getClassLoader(), "BeansTest$SimpleBean", null);
        check(b3 instanceof SimpleBean, "null BeanContext accepted");
    }

    static void testDesignModeStateIndependence() {
        try {
            Beans.setDesignTime(true);
            check(Beans.isDesignTime(), "Beans design time on");
            BeanContextSupport ctx = new BeanContextSupport();
            check(!ctx.isDesignTime(), "context flag not seeded from Beans (probed: independent)");
            ctx.setDesignTime(true);
            check(ctx.isDesignTime(), "context setDesignTime round-trip on its own flag");
            check(Beans.isDesignTime(), "context flag does not leak into Beans global flag");
        } finally {
            Beans.setDesignTime(false);
        }
        check(!Beans.isDesignTime(), "Beans design time restored to false");
    }

    static void testVersionSpecificSignatures() throws Exception {
        boolean appletInitializerPresent;
        try {
            Class.forName("java.beans.AppletInitializer");
            appletInitializerPresent = true;
        } catch (ClassNotFoundException ex) {
            appletInitializerPresent = false;
        }
        check(!appletInitializerPresent, "JDK-27: java.beans.AppletInitializer removed in JDK-9");
        boolean fourArg = false;
        int count = 0;
        for (Method m : Beans.class.getDeclaredMethods()) {
            if (m.getName().equals("instantiate")) {
                count++;
                if (m.getParameterTypes().length == 4) fourArg = true;
            }
        }
        check(!fourArg, "JDK-27: no 4-arg instantiate overload");
        check(count == 2, "JDK-27: exactly 2 instantiate overloads");
        Method sdt = Beans.class.getDeclaredMethod("setDesignTime", boolean.class);
        check(sdt.getExceptionTypes().length == 0, "JDK-27: setDesignTime declares no checked exceptions");
        Method sga = Beans.class.getDeclaredMethod("setGuiAvailable", boolean.class);
        check(sga.getExceptionTypes().length == 0, "JDK-27: setGuiAvailable declares no checked exceptions");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxStructure();
        testInstanceOfMatrix();
        testGetInstanceOf();
        testDesignTimeToggle();
        testGuiAvailableRelative();
        testInstantiatePositive();
        testInstantiateNegative();
        testInstantiateWithBeanContext();
        testDesignModeStateIndependence();
        testVersionSpecificSignatures();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
