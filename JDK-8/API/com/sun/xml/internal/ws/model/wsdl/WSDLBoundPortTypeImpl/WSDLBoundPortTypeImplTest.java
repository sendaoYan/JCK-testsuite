/*
 * @test
 * @summary com.sun.xml.internal.ws.model.wsdl.WSDLBoundPortTypeImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSDLBoundPortTypeImplTest
 */

import java.lang.reflect.Modifier;


public class WSDLBoundPortTypeImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.wsdl.WSDLBoundPortTypeImpl", false, WSDLBoundPortTypeImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.wsdl.WSDLBoundPortTypeImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.model.wsdl.AbstractFeaturedObjectImpl"), "superclass com.sun.xml.internal.ws.model.wsdl.AbstractFeaturedObjectImpl");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLBoundPortType"), "implements com.sun.xml.internal.ws.api.model.wsdl.editable.EditableWSDLBoundPortType");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addExtension"), "method addExtension");
        check(mn.contains("addNotUnderstoodExtension"), "method addNotUnderstoodExtension");
        check(mn.contains("areRequiredExtensionsUnderstood"), "method areRequiredExtensionsUnderstood");
        check(mn.contains("freeze"), "method freeze");
        check(mn.contains("get"), "method get");
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getBindingId"), "method getBindingId");
        check(mn.contains("getBindingOperations"), "method getBindingOperations");
        check(mn.contains("getExtension"), "method getExtension");
        check(mn.contains("getFeature"), "method getFeature");
        check(mn.contains("getFeatures"), "method getFeatures");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNotUnderstoodExtensions"), "method getNotUnderstoodExtensions");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getOwner"), "method getOwner");
        check(mn.contains("getPortType"), "method getPortType");
        check(mn.contains("getPortTypeName"), "method getPortTypeName");
        check(mn.contains("getStyle"), "method getStyle");
        check(mn.contains("isDoclit"), "method isDoclit");
        check(mn.contains("isRpcLit"), "method isRpcLit");
        check(mn.contains("put"), "method put");
        check(mn.contains("setBindingId"), "method setBindingId");
        check(mn.contains("setStyle"), "method setStyle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
