/*
 * @test
 * @summary com.sun.xml.internal.ws.api.pipe.ClientTubeAssemblerContext conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ClientTubeAssemblerContextTest
 */

import java.lang.reflect.Modifier;


public class ClientTubeAssemblerContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.pipe.ClientTubeAssemblerContext", false, ClientTubeAssemblerContextTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.pipe.ClientTubeAssemblerContext"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createClientMUTube"), "method createClientMUTube");
        check(mn.contains("createDumpTube"), "method createDumpTube");
        check(mn.contains("createHandlerTube"), "method createHandlerTube");
        check(mn.contains("createSecurityTube"), "method createSecurityTube");
        check(mn.contains("createTransportTube"), "method createTransportTube");
        check(mn.contains("createValidationTube"), "method createValidationTube");
        check(mn.contains("createWsaTube"), "method createWsaTube");
        check(mn.contains("getAddress"), "method getAddress");
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getBindingProvider"), "method getBindingProvider");
        check(mn.contains("getCodec"), "method getCodec");
        check(mn.contains("getContainer"), "method getContainer");
        check(mn.contains("getPortInfo"), "method getPortInfo");
        check(mn.contains("getSEI"), "method getSEI");
        check(mn.contains("getSEIModel"), "method getSEIModel");
        check(mn.contains("getService"), "method getService");
        check(mn.contains("getWsdlModel"), "method getWsdlModel");
        check(mn.contains("setCodec"), "method setCodec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
