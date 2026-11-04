/*
 * @test
 * @summary com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLBoundPortType conformance: reflection (kind, superclass, interfaces, methods).
 * @run main EditableWSDLBoundPortTypeTest
 */

import java.lang.reflect.Modifier;


public class EditableWSDLBoundPortTypeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLBoundPortType", false, EditableWSDLBoundPortTypeTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLBoundPortType"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundPortType"), "implements com.sun.xml.internal.ws.api.model.wsdl.WSDLBoundPortType");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("freeze"), "method freeze");
        check(mn.contains("get"), "method get");
        check(mn.contains("getBindingOperations"), "method getBindingOperations");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getOwner"), "method getOwner");
        check(mn.contains("getPortType"), "method getPortType");
        check(mn.contains("put"), "method put");
        check(mn.contains("setBindingId"), "method setBindingId");
        check(mn.contains("setStyle"), "method setStyle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
