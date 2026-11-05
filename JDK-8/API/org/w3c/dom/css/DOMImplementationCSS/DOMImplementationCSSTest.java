/*
 * @test
 * @summary org.w3c.dom.css.DOMImplementationCSS conformance: reflection shape checks.
 * @run main DOMImplementationCSSTest
 */

import java.lang.reflect.*;

public class DOMImplementationCSSTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.css.DOMImplementationCSS");
        check(c != null, "org.w3c.dom.css.DOMImplementationCSS loads");
        check(c.getName().equals("org.w3c.dom.css.DOMImplementationCSS"), "binary name is org.w3c.dom.css.DOMImplementationCSS");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.css.DOMImplementationCSS is public");
        check(c.isInterface(), "org.w3c.dom.css.DOMImplementationCSS is an interface");
        check(ifaceNames(c).equals(set("org.w3c.dom.DOMImplementation")), "direct superinterfaces");
        check(declares(c, "createCSSStyleSheet"), "declares createCSSStyleSheet");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: DOMImplementationCSS contract (factory channel) ----
    static void deepContract() throws Exception {
        org.w3c.dom.DOMImplementation impl = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation();
        check(!impl.hasFeature("CSS", "2.0") && impl.getFeature("CSS", "2.0") == null, "CSS module unimplemented: hasFeature false, getFeature null");
        check(!org.w3c.dom.css.DOMImplementationCSS.class.isInstance(
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().getImplementation()), "DOMImplementation is not a DOMImplementationCSS");
        org.w3c.dom.css.DOMImplementationCSS c = (org.w3c.dom.css.DOMImplementationCSS) java.lang.reflect.Proxy.newProxyInstance(
            org.w3c.dom.css.DOMImplementationCSS.class.getClassLoader(),
            new Class<?>[]{org.w3c.dom.css.DOMImplementationCSS.class},
            new java.lang.reflect.InvocationHandler() {
                public Object invoke(Object proxy, java.lang.reflect.Method m, Object[] args) {
                    if (m.getName().equals("createCSSStyleSheet")) return null;
                    if (m.getName().equals("hasFeature")) return Boolean.FALSE;
                    return null;
                }
            });
        check(c.createCSSStyleSheet("t", "screen") == null, "factory routed through the proxy");
        check(!c.hasFeature("CSS", "2.0"), "proxy reports CSS unimplemented");
        check(c.createCSSStyleSheet("t", "screen") == null, "factory routed (null wiring)");
    }
}
