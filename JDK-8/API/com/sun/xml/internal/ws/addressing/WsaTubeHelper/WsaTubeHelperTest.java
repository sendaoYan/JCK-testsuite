/*
 * @test
 * @summary com.sun.xml.internal.ws.addressing.WsaTubeHelper conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WsaTubeHelperTest
 */

import java.lang.reflect.Modifier;


public class WsaTubeHelperTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.addressing.WsaTubeHelper", false, WsaTubeHelperTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.addressing.WsaTubeHelper"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createInvalidAddressingHeaderFault"), "method createInvalidAddressingHeaderFault");
        check(mn.contains("getEffectiveInputAction"), "method getEffectiveInputAction");
        check(mn.contains("getFaultAction"), "method getFaultAction");
        check(mn.contains("getInputAction"), "method getInputAction");
        check(mn.contains("getInvalidMapDetail"), "method getInvalidMapDetail");
        check(mn.contains("getMapRequiredDetail"), "method getMapRequiredDetail");
        check(mn.contains("getOutputAction"), "method getOutputAction");
        check(mn.contains("getProblemActionDetail"), "method getProblemActionDetail");
        check(mn.contains("getSOAPAction"), "method getSOAPAction");
        check(mn.contains("isInputActionDefault"), "method isInputActionDefault");
        check(mn.contains("newMapRequiredFault"), "method newMapRequiredFault");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("addVer"), "field addVer");
        check(fn.contains("binding"), "field binding");
        check(fn.contains("seiModel"), "field seiModel");
        check(fn.contains("soapVer"), "field soapVer");
        check(fn.contains("wsdlPort"), "field wsdlPort");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
