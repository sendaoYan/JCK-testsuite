/*
 * @test
 * @summary com.sun.xml.internal.ws.api.pipe.ServerTubeAssemblerContext conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ServerTubeAssemblerContextTest
 */

import java.lang.reflect.Modifier;


public class ServerTubeAssemblerContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.pipe.ServerTubeAssemblerContext", false, ServerTubeAssemblerContextTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.pipe.ServerTubeAssemblerContext"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createDumpTube"), "method createDumpTube");
        check(mn.contains("createHandlerTube"), "method createHandlerTube");
        check(mn.contains("createMonitoringTube"), "method createMonitoringTube");
        check(mn.contains("createSecurityTube"), "method createSecurityTube");
        check(mn.contains("createServerMUTube"), "method createServerMUTube");
        check(mn.contains("createValidationTube"), "method createValidationTube");
        check(mn.contains("createWsaTube"), "method createWsaTube");
        check(mn.contains("getCodec"), "method getCodec");
        check(mn.contains("getEndpoint"), "method getEndpoint");
        check(mn.contains("getSEIModel"), "method getSEIModel");
        check(mn.contains("getTerminalTube"), "method getTerminalTube");
        check(mn.contains("getWsdlModel"), "method getWsdlModel");
        check(mn.contains("isSynchronous"), "method isSynchronous");
        check(mn.contains("setCodec"), "method setCodec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
