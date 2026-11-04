/*
 * @test
 * @summary java.beans.DesignMode conformance: exact member set via
 *          reflection (PROPERTYNAME constant value/modifiers, exactly two
 *          abstract methods, no defaults), nested state-carrying
 *          implementation round-trip (last-set-wins, idempotent sets,
 *          independent instances), statelessness of the interface itself
 *          (Beans global toggle does not reach a plain implementation),
 *          BeanContextSupport integration (implements DesignMode, own flag
 *          independent of the Beans global flag - probed on both JDKs),
 *          and cross-type assignability.
 * @run main DesignModeTest
 */

import java.beans.Beans;
import java.beans.Customizer;
import java.beans.DesignMode;
import java.beans.Visibility;
import java.beans.beancontext.BeanContextSupport;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EventListener;

public class DesignModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** State-carrying implementation of the (stateless) DesignMode contract. */
    public static class RecordingDesignMode implements DesignMode {
        boolean designTime;
        public void setDesignTime(boolean b) { designTime = b; }
        public boolean isDesignTime() { return designTime; }
    }

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = DesignMode.class;
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(c.getTypeParameters().length == 0, "not generic");
        check(c.getDeclaredAnnotations().length == 0, "no declared annotations");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "exactly 2 declared methods");
        boolean hasIs = false, hasSet = false;
        for (Method m : ms) {
            if (m.getName().equals("isDesignTime")) hasIs = true;
            if (m.getName().equals("setDesignTime")) hasSet = true;
        }
        check(hasIs && hasSet, "member set is exactly isDesignTime/setDesignTime");
        Method set = c.getDeclaredMethod("setDesignTime", boolean.class);
        check(set.getReturnType() == void.class, "setDesignTime returns void");
        check(set.getParameterTypes().length == 1
                && set.getParameterTypes()[0] == boolean.class, "setDesignTime takes one boolean");
        Method is = c.getDeclaredMethod("isDesignTime");
        check(is.getReturnType() == boolean.class, "isDesignTime returns boolean");
        check(is.getParameterTypes().length == 0, "isDesignTime takes no arguments");
        for (Method m : ms) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    m.getName() + " is public abstract");
            check(!m.isDefault(), m.getName() + " has no default body");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no exceptions");
            check(m.getDeclaredAnnotations().length == 0, m.getName() + " carries no annotations");
        }
        Field p = c.getField("PROPERTYNAME");
        check(Modifier.isPublic(p.getModifiers()) && Modifier.isStatic(p.getModifiers())
                && Modifier.isFinal(p.getModifiers()), "PROPERTYNAME is public static final");
        check(p.getType() == String.class, "PROPERTYNAME is a String");
        check("designTime".equals(p.get(null)), "PROPERTYNAME value is designTime");
        check("designTime".equals(DesignMode.PROPERTYNAME), "PROPERTYNAME accessible as a constant");
        check(c.getDeclaredFields().length == 1, "PROPERTYNAME is the only declared field");
    }

    static void testImplementationRoundTrip() {
        RecordingDesignMode dm = new RecordingDesignMode();
        check(!dm.isDesignTime(), "fresh implementation reports false");
        dm.setDesignTime(true);
        check(dm.isDesignTime(), "setDesignTime(true) stored");
        dm.setDesignTime(true);
        check(dm.isDesignTime(), "setDesignTime(true) idempotent");
        dm.setDesignTime(false);
        check(!dm.isDesignTime(), "setDesignTime(false) stored");
        dm.setDesignTime(true);
        dm.setDesignTime(false);
        dm.setDesignTime(true);
        check(dm.isDesignTime(), "last set wins over the sequence");
        RecordingDesignMode other = new RecordingDesignMode();
        other.setDesignTime(true);
        check(other.isDesignTime(), "second instance holds its own flag");
        check(dm.isDesignTime(), "first instance unchanged by second instance");
    }

    static void testInterfaceIsStateless() {
        RecordingDesignMode dm = new RecordingDesignMode();
        try {
            Beans.setDesignTime(true);
            check(Beans.isDesignTime(), "Beans global design-time toggled on");
            check(!dm.isDesignTime(), "interface itself carries no state (impl flag untouched)");
            RecordingDesignMode fresh = new RecordingDesignMode();
            check(!fresh.isDesignTime(), "fresh impl default false even with Beans design-time on");
        } finally {
            Beans.setDesignTime(false);
        }
        check(!Beans.isDesignTime(), "Beans design-time restored");
    }

    @SuppressWarnings("removal")
    static void testBeanContextIntegration() {
        BeanContextSupport ctx = new BeanContextSupport();
        check(ctx instanceof DesignMode, "BeanContextSupport implements DesignMode");
        check(!ctx.isDesignTime(), "fresh context reports not design time");
        ctx.setDesignTime(true);
        check(ctx.isDesignTime(), "context setDesignTime round-trip on its own flag");
        check(!Beans.isDesignTime(), "context flag does not propagate to the Beans global flag");
        try {
            Beans.setDesignTime(true);
            BeanContextSupport fresh = new BeanContextSupport();
            check(!fresh.isDesignTime(), "Beans global flag does not seed a fresh context flag (probed)");
        } finally {
            Beans.setDesignTime(false);
        }
    }

    static void testCrossTypeAssignability() {
        check(!DesignMode.class.isAssignableFrom(Customizer.class), "Customizer is not a DesignMode");
        check(!DesignMode.class.isAssignableFrom(Visibility.class), "Visibility is not a DesignMode");
        check(!Serializable.class.isAssignableFrom(DesignMode.class), "DesignMode does not extend Serializable");
        check(!EventListener.class.isAssignableFrom(DesignMode.class), "DesignMode is not an EventListener");
        check(DesignMode.class.isAssignableFrom(RecordingDesignMode.class), "implementation assignable to interface");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBoxStructure();
        testImplementationRoundTrip();
        testInterfaceIsStateless();
        testBeanContextIntegration();
        testCrossTypeAssignability();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
