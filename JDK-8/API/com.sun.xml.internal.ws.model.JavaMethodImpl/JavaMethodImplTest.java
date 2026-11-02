/*
 * @test
 * @summary com.sun.xml.internal.ws.model.JavaMethodImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JavaMethodImplTest
 */

import java.lang.reflect.Modifier;


public class JavaMethodImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.model.JavaMethodImpl", false, JavaMethodImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.model.JavaMethodImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.api.model.JavaMethod"), "implements com.sun.xml.internal.ws.api.model.JavaMethod");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getCheckedException"), "method getCheckedException");
        check(mn.contains("getCheckedExceptions"), "method getCheckedExceptions");
        check(mn.contains("getInputAction"), "method getInputAction");
        check(mn.contains("getInputParametersCount"), "method getInputParametersCount");
        check(mn.contains("getMEP"), "method getMEP");
        check(mn.contains("getMethod"), "method getMethod");
        check(mn.contains("getOperation"), "method getOperation");
        check(mn.contains("getOperationName"), "method getOperationName");
        check(mn.contains("getOperationQName"), "method getOperationQName");
        check(mn.contains("getOperationSignature"), "method getOperationSignature");
        check(mn.contains("getOutputAction"), "method getOutputAction");
        check(mn.contains("getOwner"), "method getOwner");
        check(mn.contains("getRequestMessageName"), "method getRequestMessageName");
        check(mn.contains("getRequestParameters"), "method getRequestParameters");
        check(mn.contains("getRequestPayloadName"), "method getRequestPayloadName");
        check(mn.contains("getResponseMessageName"), "method getResponseMessageName");
        check(mn.contains("getResponseParameters"), "method getResponseParameters");
        check(mn.contains("getResponsePayloadName"), "method getResponsePayloadName");
        check(mn.contains("getSEIMethod"), "method getSEIMethod");
        check(mn.contains("getSOAPAction"), "method getSOAPAction");
        check(mn.contains("isAsync"), "method isAsync");
        check(mn.contains("setOperationQName"), "method setOperationQName");
        check(mn.contains("setRequestPayloadName"), "method setRequestPayloadName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
