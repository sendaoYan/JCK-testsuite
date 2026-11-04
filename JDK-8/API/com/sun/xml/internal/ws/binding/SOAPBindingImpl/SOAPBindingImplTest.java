/*
 * @test
 * @summary com.sun.xml.internal.ws.binding.SOAPBindingImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SOAPBindingImplTest
 */

import java.lang.reflect.Modifier;


public class SOAPBindingImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.binding.SOAPBindingImpl", false, SOAPBindingImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.binding.SOAPBindingImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.binding.BindingImpl"), "superclass com.sun.xml.internal.ws.binding.BindingImpl");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.soap.SOAPBinding"), "implements javax.xml.ws.soap.SOAPBinding");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addRequiredRoles"), "method addRequiredRoles");
        check(mn.contains("getMessageFactory"), "method getMessageFactory");
        check(mn.contains("getRoles"), "method getRoles");
        check(mn.contains("getSOAPFactory"), "method getSOAPFactory");
        check(mn.contains("isMTOMEnabled"), "method isMTOMEnabled");
        check(mn.contains("setHandlerChain"), "method setHandlerChain");
        check(mn.contains("setMTOMEnabled"), "method setMTOMEnabled");
        check(mn.contains("setPortKnownHeaders"), "method setPortKnownHeaders");
        check(mn.contains("setRoles"), "method setRoles");
        check(mn.contains("understandsHeader"), "method understandsHeader");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("X_SOAP12HTTP_BINDING"), "field X_SOAP12HTTP_BINDING");
        check(fn.contains("soapVersion"), "field soapVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
