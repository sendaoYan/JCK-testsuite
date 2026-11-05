/*
 * @test
 * @summary javax.xml.bind.annotation.DomHandler conformance: interface hierarchy and methods.
 * @run main DomHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class DomHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.DomHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("createUnmarshaller"), "has method createUnmarshaller");
        check(names.contains("getElement"), "has method getElement");
        check(names.contains("marshal"), "has method marshal");
        testContract();
        testClassFacts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testClassFacts() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.DomHandler");
        check("javax.xml.bind.annotation.DomHandler".equals(c.getName()), "FQCN exact");
        check("DomHandler".equals(c.getSimpleName()), "simple name exact");
        check("javax.xml.bind.annotation".equals(c.getPackage().getName()), "package exact");
        check(c.getDeclaringClass() == null, "top-level type (not nested)");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "interface public");
        check(c.isAssignableFrom(Class.forName("javax.xml.bind.annotation.W3CDomHandler")),
                "isAssignableFrom(W3CDomHandler)");
        java.util.Set<String> pub = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) pub.add(m.getName());
        check(pub.contains("createUnmarshaller") && pub.contains("getElement") && pub.contains("marshal"),
                "public method view exposes all 3 operations");
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            check(m.getDeclaringClass() == c, m.getName() + " declared by DomHandler itself");
            check(!java.lang.reflect.Modifier.isStatic(m.getModifiers()), m.getName() + " not static");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no exceptions");
        }
    }

    static void testContract() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.DomHandler");
        check(!c.isAnnotation(), "not an annotation type");
        check(!c.isEnum(), "not an enum");
        check(java.lang.reflect.Modifier.isInterface(c.getModifiers()), "Modifier reports interface");
        java.lang.reflect.TypeVariable<?>[] tps = c.getTypeParameters();
        check(tps.length == 2, "two type parameters ElementT, ResultT");
        check("ElementT".equals(tps[0].getName()), "first type parameter named ElementT");
        check("ResultT".equals(tps[1].getName()), "second type parameter named ResultT");
        check(tps[0].getBounds().length == 1 && tps[0].getBounds()[0] == Object.class, "ElementT unbounded");
        check(tps[1].getBounds().length == 1 && tps[1].getBounds()[0] == javax.xml.transform.Result.class,
                "ResultT bounded by javax.xml.transform.Result");
        // exact method signatures (erased)
        java.lang.reflect.Method cu = c.getDeclaredMethod("createUnmarshaller", javax.xml.bind.ValidationEventHandler.class);
        check(cu.getReturnType() == javax.xml.transform.Result.class, "createUnmarshaller erased return type Result");
        check(java.lang.reflect.Modifier.isAbstract(cu.getModifiers()), "createUnmarshaller abstract");
        check(cu.getParameterTypes().length == 1, "createUnmarshaller takes 1 parameter");
        java.lang.reflect.Method ge = c.getDeclaredMethod("getElement", javax.xml.transform.Result.class);
        check(ge.getReturnType() == Object.class, "getElement erased return type Object");
        check(java.lang.reflect.Modifier.isAbstract(ge.getModifiers()), "getElement abstract");
        java.lang.reflect.Method ma = c.getDeclaredMethod("marshal", Object.class, javax.xml.bind.ValidationEventHandler.class);
        check(ma.getReturnType() == javax.xml.transform.Source.class, "marshal erased return type Source");
        check(java.lang.reflect.Modifier.isAbstract(ma.getModifiers()), "marshal abstract");
        check(java.lang.reflect.Modifier.isPublic(ma.getModifiers()), "marshal public");
        check(c.getDeclaredMethods().length == 3, "exactly 3 abstract methods");
        check(c.getDeclaredFields().length == 0, "no fields");
        check(c.getDeclaredConstructors().length == 0, "no constructors (interface)");
        // reference implementation binding
        Class<?> w3c = Class.forName("javax.xml.bind.annotation.W3CDomHandler");
        check(w3c.getInterfaces().length == 1 && w3c.getInterfaces()[0] == c, "W3CDomHandler implements DomHandler (erased)");
        // a custom handler implementation is straightforward
        javax.xml.bind.annotation.DomHandler<org.w3c.dom.Element, javax.xml.transform.dom.DOMResult> custom =
                new javax.xml.bind.annotation.W3CDomHandler();
        javax.xml.transform.dom.DOMResult r = custom.createUnmarshaller(null);
        check(r != null, "custom-typed handler createUnmarshaller works");
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        org.w3c.dom.Element el = doc.createElement("e");
        doc.appendChild(el);
        javax.xml.transform.dom.DOMResult dr = new javax.xml.transform.dom.DOMResult(doc);
        check(custom.getElement(dr) == el, "custom-typed handler getElement resolves document element");
        javax.xml.transform.Source s = custom.marshal(el, null);
        check(s != null, "custom-typed handler marshal works");
    }
}
