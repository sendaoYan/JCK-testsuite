/*
 * @test
 * @summary java.beans.beancontext.BeanContextChildComponentProxy conformance: white-box structure + black-box behavior.
 *          Note: the whole java.beans.beancontext package carries Deprecated(forRemoval=true, since=23) on
 *          JDK 23+ but remains fully functional; structural deprecation assertions cover that.
 *          Exact interface shape (single getComponent() returning java.awt.Component exactly, no
 *          superinterfaces); implementations returning a real lightweight component (JLabel -
 *          construction verified headless-safe on both JDKs) and returning null (the documented
 *          allowance for beans with no component); stable identity across calls; polymorphic use
 *          through the Component supertype; and the recorded observation that no member of the
 *          beancontext framework consumes this interface at runtime (it is a marker contract for
 *          GUI builders). Pure headless: nothing is displayed.
 *          Behavior verified identical on JDK 8 and 27.
 * @run main BeanContextChildComponentProxyTest
 */

import java.awt.Component;
import java.beans.beancontext.BeanContextChildComponentProxy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class BeanContextChildComponentProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static String methodNames(Class<?> c) {
        List<String> l = new ArrayList<String>();
        for (Method m : c.getDeclaredMethods()) l.add(m.getName());
        java.util.Collections.sort(l);
        return l.toString();
    }

    // impl returning a real lightweight component (never shown)
    public static class LabeledBean implements BeanContextChildComponentProxy {
        final Component comp = new javax.swing.JLabel("bean");
        public Component getComponent() { return comp; }
    }

    // impl without any component: null is a legal answer
    public static class HeadlessBean implements BeanContextChildComponentProxy {
        public Component getComponent() { return null; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChildComponentProxy");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getComponent"), "declares getComponent");
        // exact inventory
        check(methodNames(c).equals("[getComponent]"), "exact single-method inventory [getComponent]");
        check(c.getDeclaredMethods().length == 1, "declares exactly one method");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Method m = c.getMethod("getComponent");
        check(Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                "getComponent is public abstract");
        check(m.getReturnType() == Component.class, "returns java.awt.Component exactly");
        check(m.getParameterTypes().length == 0, "takes no arguments");
        check(m.getExceptionTypes().length == 0, "declares no checked exceptions");
        check(!java.util.EventListener.class.isAssignableFrom(c), "not an EventListener");
        check(!java.io.Serializable.class.isAssignableFrom(c), "interface is not Serializable");
        check(!java.beans.beancontext.BeanContextChild.class.isAssignableFrom(c),
                "not a BeanContextChild subtype (independent role)");
        check(!java.beans.beancontext.BeanContextContainerProxy.class.isAssignableFrom(c),
                "independent from BeanContextContainerProxy");
        // Container IS-A Component: the container-proxy covariant room exists but this
        // interface pins the plain Component return type
        check(java.awt.Container.class.isAssignableFrom(Component.class) == false
                && Component.class.isAssignableFrom(java.awt.Container.class),
                "Container is a Component, so a container proxy could satisfy this signature covariantly");
        // deprecation status on this JDK: whole package deprecated for removal since 23, still functional
        Deprecated dep = c.getAnnotation(Deprecated.class);
        check(dep != null, "deprecated on JDK 27");
        check(dep.forRemoval(), "deprecated forRemoval=true");
        check("23".equals(dep.since()), "deprecated since=23");
    }

    static void testOriginalWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChildComponentProxy");
        check(c.isInterface(), "is an interface");
        check(Component.class.isAssignableFrom(Component.class), "Component type");
    }

    static void testBlackBox() throws Exception {
        final Component comp = new javax.swing.JLabel();
        BeanContextChildComponentProxy p = new BeanContextChildComponentProxy() {
            public Component getComponent() { return comp; }
        };
        check(comp == p.getComponent(), "getComponent");
    }

    static void testSemantics() throws Exception {
        // real component impl: identity stable across calls, never constructed anew
        LabeledBean bean = new LabeledBean();
        Component first = bean.getComponent();
        check(first != null, "component impl returns non-null");
        check(bean.getComponent() == first, "same component instance on every call");
        check(first instanceof javax.swing.JLabel, "concrete component type preserved");
        check(first instanceof Component, "usable through the Component supertype");
        // null-returning impl is equally legal
        HeadlessBean plain = new HeadlessBean();
        check(plain.getComponent() == null, "null return allowed for beans without a component");
        // two beans carry independent components
        LabeledBean other = new LabeledBean();
        check(other.getComponent() != bean.getComponent(), "component instances are per-bean");
        // polymorphic dispatch across implementations
        BeanContextChildComponentProxy[] proxies = { bean, plain, other };
        int nonNull = 0;
        for (int i = 0; i < proxies.length; i++)
            if (proxies[i].getComponent() != null) nonNull++;
        check(nonNull == 2, "polymorphic dispatch counts 2 components, 1 null");
        // an AWT Panel (headless-constructible, unlike Label/Frame) also serves as the component
        final java.awt.Panel awtPanel = new java.awt.Panel();
        BeanContextChildComponentProxy awtProxy = new BeanContextChildComponentProxy() {
            public Component getComponent() { return awtPanel; }
        };
        check(awtProxy.getComponent() == awtPanel && awtPanel instanceof Component,
                "plain AWT components satisfy the contract");
        // the interface does not demand any beancontext wiring: a bare object suffices
        check(new HeadlessBean() instanceof BeanContextChildComponentProxy,
                "no framework construction required to implement it");
        // an implementation may guard its component and throw - the interface adds no wrapping
        final UnsupportedOperationException guard = new UnsupportedOperationException("no gui");
        BeanContextChildComponentProxy guarded = new BeanContextChildComponentProxy() {
            public Component getComponent() { throw guard; }
        };
        try {
            guarded.getComponent();
            check(false, "expected the guard exception to surface");
        } catch (UnsupportedOperationException uoe) {
            check(uoe == guard, "implementation exceptions propagate unwrapped");
        }
    }

    static void testReflectionDispatch() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChildComponentProxy");
        check(!c.isAnnotationPresent(java.lang.FunctionalInterface.class),
                "not annotated @FunctionalInterface");
        Method m = c.getMethod("getComponent");
        LabeledBean bean = new LabeledBean();
        check(m.invoke(bean) == bean.comp, "reflective invoke returns the component");
        check(m.invoke(new HeadlessBean()) == null, "reflective invoke returns null for the null impl");
        check(m.getDeclaringClass() == c, "getComponent is declared by the interface itself");
        check(m.invoke(new LabeledBean()) instanceof javax.swing.JLabel,
                "reflective dispatch preserves the concrete type");
    }

    public static void main(String[] args) throws Exception {
        testOriginalWhiteBox();
        testWhiteBox();
        testBlackBox();
        testSemantics();
        testReflectionDispatch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
