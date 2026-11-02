/*
 * @test
 * @summary com.sun.xml.internal.ws.model.wsdl.WSDLMessageImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLMessageImplTest
 */

import java.lang.reflect.Modifier;


public class WSDLMessageImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.wsdl.WSDLMessageImpl", false, WSDLMessageImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.wsdl.WSDLMessageImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl"), "superclass com.sun.xml.internal.ws.model.wsdl.AbstractExtensibleImpl");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLMessage"), "implements com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLMessage");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("add"), "method add");
        check(mn.contains("addExtension"), "method addExtension");
        check(mn.contains("addNotUnderstoodExtension"), "method addNotUnderstoodExtension");
        check(mn.contains("areRequiredExtensionsUnderstood"), "method areRequiredExtensionsUnderstood");
        check(mn.contains("getExtension"), "method getExtension");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNotUnderstoodExtensions"), "method getNotUnderstoodExtensions");
        check(mn.contains("parts"), "method parts");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
