/*
 * @test
 * @summary com.sun.xml.internal.ws.model.wsdl.WSDLPartImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLPartImplTest
 */

import java.lang.reflect.Modifier;


public class WSDLPartImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.wsdl.WSDLPartImpl", false, WSDLPartImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.wsdl.WSDLPartImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.model.wsdl.AbstractObjectImpl"), "superclass com.sun.xml.internal.ws.model.wsdl.AbstractObjectImpl");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLPart"), "implements com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLPart");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getDescriptor"), "method getDescriptor");
        check(mn.contains("getIndex"), "method getIndex");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("setBinding"), "method setBinding");
        check(mn.contains("setIndex"), "method setIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
