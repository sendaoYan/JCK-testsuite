/*
 * @test
 * @summary com.sun.xml.internal.ws.api.pipe.ClientPipeAssemblerContext conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ClientPipeAssemblerContextTest
 */

import java.lang.reflect.Modifier;


public class ClientPipeAssemblerContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.pipe.ClientPipeAssemblerContext", false, ClientPipeAssemblerContextTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.pipe.ClientPipeAssemblerContext"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.api.pipe.ClientTubeAssemblerContext"), "superclass com.sun.xml.internal.ws.api.pipe.ClientTubeAssemblerContext");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createClientMUPipe"), "method createClientMUPipe");
        check(mn.contains("createDumpPipe"), "method createDumpPipe");
        check(mn.contains("createHandlerPipe"), "method createHandlerPipe");
        check(mn.contains("createSecurityPipe"), "method createSecurityPipe");
        check(mn.contains("createTransportPipe"), "method createTransportPipe");
        check(mn.contains("createValidationPipe"), "method createValidationPipe");
        check(mn.contains("createWsaPipe"), "method createWsaPipe");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
