/*
 * @test
 * @summary com.sun.xml.internal.ws.model.wsdl.WSDLServiceImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLServiceImplTest
 */

import java.lang.reflect.Modifier;


public class WSDLServiceImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.wsdl.WSDLServiceImpl", false, WSDLServiceImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.wsdl.WSDLServiceImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl"), "superclass com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLService"), "implements com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLService");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addExtension"), "method addExtension");
        check(mn.contains("addNotUnderstoodExtension"), "method addNotUnderstoodExtension");
        check(mn.contains("areRequiredExtensionsUnderstood"), "method areRequiredExtensionsUnderstood");
        check(mn.contains("freeze"), "method freeze");
        check(mn.contains("get"), "method get");
        check(mn.contains("getExtension"), "method getExtension");
        check(mn.contains("getFirstPort"), "method getFirstPort");
        check(mn.contains("getMatchingPort"), "method getMatchingPort");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNotUnderstoodExtensions"), "method getNotUnderstoodExtensions");
        check(mn.contains("getParent"), "method getParent");
        check(mn.contains("getPorts"), "method getPorts");
        check(mn.contains("put"), "method put");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
