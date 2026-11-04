/*
 * @test
 * @summary com.sun.xml.internal.ws.api.model.SEIModel conformance: reflection (kind, superclass, interfaces, methods).
 * @run main SEIModelTest
 */

import java.lang.reflect.Modifier;


public class SEIModelTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.model.SEIModel", false, SEIModelTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.model.SEIModel"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getBoundPortTypeName"), "method getBoundPortTypeName");
        check(mn.contains("getJAXBContext"), "method getJAXBContext");
        check(mn.contains("getJavaMethod"), "method getJavaMethod");
        check(mn.contains("getJavaMethodForWsdlOperation"), "method getJavaMethodForWsdlOperation");
        check(mn.contains("getJavaMethods"), "method getJavaMethods");
        check(mn.contains("getMarshallerPool"), "method getMarshallerPool");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("getPortTypeName"), "method getPortTypeName");
        check(mn.contains("getServiceQName"), "method getServiceQName");
        check(mn.contains("getTargetNamespace"), "method getTargetNamespace");
        check(mn.contains("getWSDLLocation"), "method getWSDLLocation");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
