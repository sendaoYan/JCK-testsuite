/*
 * @test
 * @summary com.sun.xml.internal.ws.assembler.dev.ServerTubelineAssemblyContext conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ServerTubelineAssemblyContextTest
 */

import java.lang.reflect.Modifier;


public class ServerTubelineAssemblyContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.assembler.dev.ServerTubelineAssemblyContext", false, ServerTubelineAssemblyContextTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.assembler.dev.ServerTubelineAssemblyContext"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.assembler.dev.TubelineAssemblyContext"), "implements com.sun.xml.internal.ws.assembler.dev.TubelineAssemblyContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getCodec"), "method getCodec");
        check(mn.contains("getEndpoint"), "method getEndpoint");
        check(mn.contains("getPolicyMap"), "method getPolicyMap");
        check(mn.contains("getSEIModel"), "method getSEIModel");
        check(mn.contains("getTerminalTube"), "method getTerminalTube");
        check(mn.contains("getWrappedContext"), "method getWrappedContext");
        check(mn.contains("getWsdlPort"), "method getWsdlPort");
        check(mn.contains("isPolicyAvailable"), "method isPolicyAvailable");
        check(mn.contains("isSynchronous"), "method isSynchronous");
        check(mn.contains("setCodec"), "method setCodec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
