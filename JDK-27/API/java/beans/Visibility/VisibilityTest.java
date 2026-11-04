/*
 * @test
 * @summary java.beans.Visibility conformance: exact member set via
 *          reflection (four abstract methods needsGui / dontUseGui /
 *          okToUseGui / avoidingGui, no defaults), GUI-needing bean
 *          matrix (needsGui stays true across dontUseGui/okToUseGui,
 *          avoidingGui flips), headless bean matrix (needsGui false
 *          invariant, avoidingGui only after dontUseGui), instance
 *          independence, BeanContextSupport integration (context extends
 *          Visibility, nested beans keep their visibility semantics), and
 *          cross-type assignability.
 * @run main VisibilityTest
 */

import java.beans.Customizer;
import java.beans.DesignMode;
import java.beans.Visibility;
import java.beans.beancontext.BeanContextSupport;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EventListener;

public class VisibilityTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Bean that intrinsically needs a GUI (e.g. a drawn component). */
    public static class GuiBean implements Visibility {
        boolean guiAllowed = true;
        public boolean needsGui() { return true; }
        public void dontUseGui() { guiAllowed = false; }
        public void okToUseGui() { guiAllowed = true; }
        public boolean avoidingGui() { return !guiAllowed; }
    }

    /** Bean that never needs a GUI and only avoids it once told. */
    public static class HeadlessBean implements Visibility {
        boolean avoiding;
        public boolean needsGui() { return false; }
        public void dontUseGui() { avoiding = true; }
        public void okToUseGui() { avoiding = false; }
        public boolean avoidingGui() { return avoiding; }
    }

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Visibility.class;
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(c.getTypeParameters().length == 0, "not generic");
        check(c.getDeclaredAnnotations().length == 0, "no declared annotations");
        check(c.getDeclaredFields().length == 0, "no declared fields (no constants)");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 4, "exactly 4 declared methods");
        boolean needs = false, dont = false, ok = false, avoiding = false;
        for (Method m : ms) {
            if (m.getName().equals("needsGui")) needs = true;
            if (m.getName().equals("dontUseGui")) dont = true;
            if (m.getName().equals("okToUseGui")) ok = true;
            if (m.getName().equals("avoidingGui")) avoiding = true;
        }
        check(needs && dont && ok && avoiding, "member set is the exact 4-method protocol");
        Method n = c.getDeclaredMethod("needsGui");
        check(n.getReturnType() == boolean.class && n.getParameterTypes().length == 0,
                "needsGui(): boolean, no args");
        Method d = c.getDeclaredMethod("dontUseGui");
        check(d.getReturnType() == void.class && d.getParameterTypes().length == 0,
                "dontUseGui(): void, no args");
        Method o = c.getDeclaredMethod("okToUseGui");
        check(o.getReturnType() == void.class && o.getParameterTypes().length == 0,
                "okToUseGui(): void, no args");
        Method a = c.getDeclaredMethod("avoidingGui");
        check(a.getReturnType() == boolean.class && a.getParameterTypes().length == 0,
                "avoidingGui(): boolean, no args");
        for (Method m : ms) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    m.getName() + " is public abstract");
            check(!m.isDefault(), m.getName() + " has no default body");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no exceptions");
            check(m.getDeclaredAnnotations().length == 0, m.getName() + " carries no annotations");
        }
    }

    static void testGuiBeanMatrix() {
        GuiBean b = new GuiBean();
        check(b.needsGui(), "GUI bean needs a gui");
        check(!b.avoidingGui(), "GUI bean not avoiding before dontUseGui");
        b.dontUseGui();
        check(b.avoidingGui(), "dontUseGui makes the bean avoid the gui");
        check(b.needsGui(), "needsGui is intrinsic and stays true after dontUseGui");
        b.okToUseGui();
        check(!b.avoidingGui(), "okToUseGui clears the avoiding flag");
        check(b.needsGui(), "needsGui still true after okToUseGui");
        b.dontUseGui();
        b.dontUseGui();
        check(b.avoidingGui(), "repeated dontUseGui keeps avoiding state");
        b.okToUseGui();
        check(!b.avoidingGui() && b.needsGui(), "okToUseGui restores the non-avoiding state");
    }

    static void testHeadlessBeanMatrix() {
        HeadlessBean h = new HeadlessBean();
        check(!h.needsGui(), "headless bean does not need a gui");
        check(!h.avoidingGui(), "headless bean not avoiding until told");
        h.dontUseGui();
        check(h.avoidingGui(), "dontUseGui sets the avoiding flag");
        check(!h.needsGui(), "needsGui stays false after dontUseGui");
        h.okToUseGui();
        check(!h.avoidingGui(), "okToUseGui clears the avoiding flag");
        check(!h.needsGui(), "needsGui stays false after okToUseGui");
        h.dontUseGui();
        check(h.avoidingGui() && !h.needsGui(), "final dontUseGui state consistent");
    }

    static void testInstanceIndependence() {
        GuiBean a = new GuiBean();
        GuiBean b = new GuiBean();
        a.dontUseGui();
        check(a.avoidingGui() && !b.avoidingGui(), "avoiding state is per instance");
        HeadlessBean h1 = new HeadlessBean();
        HeadlessBean h2 = new HeadlessBean();
        h1.okToUseGui();
        check(!h1.avoidingGui() && !h2.avoidingGui(), "headless instances independent");
    }

    @SuppressWarnings("removal")
    static void testBeanContextIntegration() {
        BeanContextSupport ctx = new BeanContextSupport();
        check(ctx instanceof Visibility, "BeanContextSupport implements Visibility");
        check(Visibility.class.isAssignableFrom(ctx.getClass()), "assignable check via class object");
        GuiBean g = new GuiBean();
        HeadlessBean h = new HeadlessBean();
        ctx.add(g);
        ctx.add(h);
        check(ctx.size() == 2 && ctx.contains(g) && ctx.contains(h), "both visibility beans nested in context");
        g.dontUseGui();
        check(g.avoidingGui(), "GUI bean avoids gui while nested in the context");
        check(ctx.contains(g), "membership unaffected by visibility transitions");
        check(!h.needsGui() && !h.avoidingGui(), "headless bean semantics preserved while nested");
        h.dontUseGui();
        check(h.avoidingGui(), "dontUseGui honored while nested in the context");
        check(g.needsGui(), "intrinsic needsGui preserved while nested");
        check(ctx.size() == 2, "context size unchanged by visibility calls");
    }

    static void testCrossTypeAssignability() {
        check(!Visibility.class.isAssignableFrom(DesignMode.class), "DesignMode is not a Visibility");
        check(!Visibility.class.isAssignableFrom(Customizer.class), "Customizer is not a Visibility");
        check(!Serializable.class.isAssignableFrom(Visibility.class), "Visibility does not extend Serializable");
        check(!EventListener.class.isAssignableFrom(Visibility.class), "Visibility is not an EventListener");
        check(Visibility.class.isAssignableFrom(GuiBean.class)
                && Visibility.class.isAssignableFrom(HeadlessBean.class), "implementations assignable to interface");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBoxStructure();
        testGuiBeanMatrix();
        testHeadlessBeanMatrix();
        testInstanceIndependence();
        testBeanContextIntegration();
        testCrossTypeAssignability();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
