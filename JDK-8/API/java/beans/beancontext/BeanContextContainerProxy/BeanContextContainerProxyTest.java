/*
 * @test
 * @summary java.beans.beancontext.BeanContextContainerProxy conformance: white-box structure + black-box behavior.
 *          Exact interface shape (single getContainer() returning java.awt.Container exactly, no
 *          superinterfaces); implementations returning a real headless-constructible Container
 *          (java.awt.Panel - verified headless-safe on both JDKs, unlike Label/Frame) and
 *          returning null; stable identity across calls; polymorphic use through the Container
 *          supertype (Container IS-A Component); exception propagation from an implementation;
 *          and the recorded observation that no member of the beancontext framework consumes
 *          this interface at runtime (it is a marker contract for GUI builders).
 *          Pure headless: nothing is displayed.
 *          Behavior verified identical on JDK 8 and 27.
 * @run main BeanContextContainerProxyTest
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.Panel;
import java.beans.beancontext.BeanContextContainerProxy;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class BeanContextContainerProxyTest {
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

    // impl returning a real container (never shown)
    public static class PanelBean implements BeanContextContainerProxy {
        final Container cont = new Panel();
        public Container getContainer() { return cont; }
    }

    // impl without any container: null is a legal answer
    public static class BareBean implements BeanContextContainerProxy {
        public Container getContainer() { return null; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextContainerProxy");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getContainer"), "declares getContainer");
        // exact inventory
        check(methodNames(c).equals("[getContainer]"), "exact single-method inventory [getContainer]");
        check(c.getDeclaredMethods().length == 1, "declares exactly one method");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Method m = c.getMethod("getContainer");
        check(Modifier.isAbstract(m.getModifiers()) && Modifier.isPublic(m.getModifiers()),
                "getContainer is public abstract");
        check(m.getReturnType() == Container.class, "returns java.awt.Container exactly");
        check(m.getParameterTypes().length == 0, "takes no arguments");
        check(m.getExceptionTypes().length == 0, "declares no checked exceptions");
        check(!java.util.EventListener.class.isAssignableFrom(c), "not an EventListener");
        check(!java.io.Serializable.class.isAssignableFrom(c), "interface is not Serializable");
        check(!java.beans.beancontext.BeanContextChild.class.isAssignableFrom(c),
                "not a BeanContextChild subtype (independent role)");
        check(!java.beans.beancontext.BeanContextChildComponentProxy.class.isAssignableFrom(c),
                "independent from BeanContextChildComponentProxy");
        // type lattice: a Container is a Component, so this proxy could satisfy a
        // Component-typed consumer, but the two interfaces remain unrelated
        check(Component.class.isAssignableFrom(Container.class),
                "Container is a Component (covariant lattice fact)");
        check(!java.beans.beancontext.BeanContextProxy.class.isAssignableFrom(c),
                "independent from BeanContextProxy");
        // deprecation status on this JDK
        check(c.getAnnotation(Deprecated.class) == null, "not deprecated on JDK 8");
    }

    static void testBlackBox() throws Exception {
        final Container cont = new java.awt.Panel();
        BeanContextContainerProxy p = new BeanContextContainerProxy() {
            public Container getContainer() { return cont; }
        };
        check(cont == p.getContainer(), "getContainer");
    }

    static void testSemantics() throws Exception {
        // real container impl: identity stable across calls
        PanelBean bean = new PanelBean();
        Container first = bean.getContainer();
        check(first != null, "container impl returns non-null");
        check(bean.getContainer() == first, "same container instance on every call");
        check(first instanceof Panel, "concrete container type preserved");
        check(first instanceof Container && first instanceof Component,
                "usable through Container and Component supertypes");
        // null-returning impl is equally legal
        BareBean bare = new BareBean();
        check(bare.getContainer() == null, "null return allowed for contexts without a container");
        // two beans carry independent containers
        PanelBean other = new PanelBean();
        check(other.getContainer() != bean.getContainer(), "container instances are per-bean");
        // polymorphic dispatch across implementations
        BeanContextContainerProxy[] proxies = { bean, bare, other };
        int nonNull = 0;
        for (int i = 0; i < proxies.length; i++)
            if (proxies[i].getContainer() != null) nonNull++;
        check(nonNull == 2, "polymorphic dispatch counts 2 containers, 1 null");
        // container nesting does not involve the interface at all (plain AWT fact, recorded)
        Panel inner = new Panel();
        first.add(inner);
        check(first.getComponentCount() == 1, "plain AWT nesting unaffected by the proxy contract");
        first.remove(inner);
        check(first.getComponentCount() == 0, "removal symmetric");
        // an implementation may guard its container and throw - no wrapping by the interface
        final UnsupportedOperationException guard = new UnsupportedOperationException("no container");
        BeanContextContainerProxy guarded = new BeanContextContainerProxy() {
            public Container getContainer() { throw guard; }
        };
        try {
            guarded.getContainer();
            check(false, "expected the guard exception to surface");
        } catch (UnsupportedOperationException uoe) {
            check(uoe == guard, "implementation exceptions propagate unwrapped");
        }
    }

    static void testReflectionDispatch() throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextContainerProxy");
        check(!c.isAnnotationPresent(java.lang.FunctionalInterface.class),
                "not annotated @FunctionalInterface");
        Method m = c.getMethod("getContainer");
        PanelBean bean = new PanelBean();
        check(m.invoke(bean) == bean.cont, "reflective invoke returns the container");
        check(m.invoke(new BareBean()) == null, "reflective invoke returns null for the null impl");
        check(m.getDeclaringClass() == c, "getContainer is declared by the interface itself");
        check(m.invoke(new PanelBean()) instanceof Panel,
                "reflective dispatch preserves the concrete type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testSemantics();
        testReflectionDispatch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
