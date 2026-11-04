/*
 * @test
 * @summary com.sun.xml.internal.ws.model.wsdl.WSDLFaultImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLFaultImplTest
 */

import java.lang.reflect.Modifier;


public class WSDLFaultImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.wsdl.WSDLFaultImpl", false, WSDLFaultImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.wsdl.WSDLFaultImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl"), "superclass com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLFault"), "implements com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLFault");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addExtension"), "method addExtension");
        check(mn.contains("addNotUnderstoodExtension"), "method addNotUnderstoodExtension");
        check(mn.contains("areRequiredExtensionsUnderstood"), "method areRequiredExtensionsUnderstood");
        check(mn.contains("freeze"), "method freeze");
        check(mn.contains("getAction"), "method getAction");
        check(mn.contains("getExtension"), "method getExtension");
        check(mn.contains("getMessage"), "method getMessage");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNotUnderstoodExtensions"), "method getNotUnderstoodExtensions");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getQName"), "method getQName");
        check(mn.contains("isDefaultAction"), "method isDefaultAction");
        check(mn.contains("setAction"), "method setAction");
        check(mn.contains("setDefaultAction"), "method setDefaultAction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
