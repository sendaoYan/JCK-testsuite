/*
 * @test
 * @summary java.beans.AppletInitializer conformance (JDK-8 only; the
 *          interface was removed in JDK-9 together with applet support):
 *          reflection inventory (interface, no superinterfaces, exactly 2
 *          abstract methods initialize(Applet,BeanContext) /
 *          activate(Applet) with exact parameter types, void returns, no
 *          annotations, NOT deprecated), nested recording implementation
 *          (initialize/activate invocation counts and argument identity,
 *          nulls accepted), assignability matrix (not a BeanContext /
 *          Customizer / Visibility / DesignMode / Serializable /
 *          EventListener), dual implementation together with Customizer,
 *          and the Beans 4-arg instantiate overload referencing this type.
 *          Headless: no Applet instance is ever constructed; only null
 *          applet arguments and reflection metadata are used.
 * @run main AppletInitializerTest
 */

import java.beans.AppletInitializer;
import java.beans.Beans;
import java.beans.Customizer;
import java.beans.DesignMode;
import java.beans.PropertyChangeListener;
import java.beans.Visibility;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextSupport;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EventListener;

public class AppletInitializerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
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

    /** Records initialize/activate invocations; accepts null arguments. */
    public static class RecordingInitializer implements AppletInitializer {
        int initCount, activateCount;
        java.applet.Applet lastApplet;
        BeanContext lastContext;
        public void initialize(java.applet.Applet applet, BeanContext context) {
            initCount++;
            lastApplet = applet;
            lastContext = context;
        }
        public void activate(java.applet.Applet applet) {
            activateCount++;
            lastApplet = applet;
        }
    }

    /** One class may implement AppletInitializer and Customizer together. */
    public static class DualImpl implements AppletInitializer, Customizer {
        Object target;
        public void initialize(java.applet.Applet a, BeanContext c) {}
        public void activate(java.applet.Applet a) {}
        public void setObject(Object o) { target = o; }
        public void addPropertyChangeListener(PropertyChangeListener l) {}
        public void removePropertyChangeListener(PropertyChangeListener l) {}
    }

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Class.forName("java.beans.AppletInitializer");
        check(c == AppletInitializer.class, "Class.forName returns the compile-time class object");
        check(Class.forName("java.beans.AppletInitializer") == AppletInitializer.class,
                "repeated lookup returns the same Class object");
        check(c.isInterface(), "interface");
        check(Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(c.getTypeParameters().length == 0, "not generic");
        check(c.getDeclaredAnnotations().length == 0, "no declared annotations");
        check(c.getDeclaredAnnotation(Deprecated.class) == null, "not deprecated in JDK-8");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "exactly 2 declared methods");
        boolean hasInit = false, hasActivate = false;
        for (Method m : ms) {
            if (m.getName().equals("initialize")) hasInit = true;
            if (m.getName().equals("activate")) hasActivate = true;
        }
        check(hasInit && hasActivate, "member set is exactly initialize/activate");
        Class<?> applet = Class.forName("java.applet.Applet");
        Method init = c.getDeclaredMethod("initialize", applet, BeanContext.class);
        check(init != null, "initialize(Applet, BeanContext) declared with exact parameter types");
        check(init.getReturnType() == void.class, "initialize returns void");
        check(init.getParameterTypes().length == 2, "initialize takes exactly two arguments");
        check(init.getParameterTypes()[0] == applet, "initialize first parameter is java.applet.Applet");
        check(init.getParameterTypes()[1] == BeanContext.class, "initialize second parameter is BeanContext");
        Method act = c.getDeclaredMethod("activate", applet);
        check(act != null, "activate(Applet) declared with exact parameter type");
        check(act.getReturnType() == void.class, "activate returns void");
        check(act.getParameterTypes().length == 1, "activate takes exactly one argument");
        check(act.getParameterTypes()[0] == applet, "activate parameter is java.applet.Applet");
        checkThrows(NoSuchMethodException.class,
                () -> c.getDeclaredMethod("initialize", String.class, BeanContext.class),
                "initialize with wrong first parameter rejected");
        checkThrows(NoSuchMethodException.class,
                () -> c.getDeclaredMethod("activate", Object.class),
                "activate with widened parameter rejected");
        for (Method m : ms) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    m.getName() + " is public abstract");
            check(!m.isDefault(), m.getName() + " has no default body");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no exceptions");
            check(m.getDeclaredAnnotations().length == 0, m.getName() + " carries no annotations");
        }
        check(!applet.isInterface() && Modifier.isPublic(applet.getModifiers()),
                "java.applet.Applet is a public class (parameter type anchor)");
        check("java.awt.Panel".equals(applet.getSuperclass().getName()), "Applet extends java.awt.Panel");
        check(Serializable.class.isAssignableFrom(applet), "Applet is serializable");
    }

    static void testRecordingImplementation() {
        RecordingInitializer r = new RecordingInitializer();
        check(r.initCount == 0 && r.activateCount == 0, "fresh recorder has zero invocation counts");
        r.initialize(null, null);
        check(r.initCount == 1, "initialize counted");
        check(r.lastApplet == null && r.lastContext == null, "initialize(null, null) accepted and recorded");
        BeanContextSupport ctx = new BeanContextSupport();
        r.initialize(null, ctx);
        check(r.initCount == 2 && r.lastContext == ctx, "initialize records context identity");
        r.activate(null);
        check(r.activateCount == 1 && r.lastApplet == null, "activate(null) accepted and recorded");
        r.initialize(null, null);
        check(r.initCount == 3 && r.lastContext == null, "last invocation wins in recorded arguments");
        RecordingInitializer other = new RecordingInitializer();
        other.activate(null);
        check(r.activateCount == 1 && other.activateCount == 1, "instances count independently");
        check(r instanceof AppletInitializer, "recorder is an AppletInitializer");
    }

    static void testAssignability() throws Exception {
        Class<?> c = AppletInitializer.class;
        check(!BeanContext.class.isAssignableFrom(c), "AppletInitializer is not a BeanContext");
        check(!Customizer.class.isAssignableFrom(c), "not a Customizer");
        check(!Visibility.class.isAssignableFrom(c), "not a Visibility");
        check(!DesignMode.class.isAssignableFrom(c), "not a DesignMode");
        check(!Serializable.class.isAssignableFrom(c), "does not extend Serializable");
        check(!EventListener.class.isAssignableFrom(c), "not an EventListener");
        DualImpl d = new DualImpl();
        check(d instanceof AppletInitializer && d instanceof Customizer, "dual implementation allowed");
        d.setObject("x");
        check("x".equals(d.target), "dual implementation Customizer side works");
        Method m4 = Beans.class.getDeclaredMethod("instantiate", ClassLoader.class, String.class,
                BeanContext.class, AppletInitializer.class);
        check(m4 != null, "Beans 4-arg instantiate overload references AppletInitializer (JDK-8 only)");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.AppletInitializer");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "activate"), "declares activate");
        check(hasMethod(c, "initialize"), "declares initialize");
        testWhiteBoxStructure();
        testRecordingImplementation();
        testAssignability();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
