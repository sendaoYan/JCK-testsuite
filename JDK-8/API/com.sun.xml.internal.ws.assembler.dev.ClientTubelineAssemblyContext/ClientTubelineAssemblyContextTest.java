/*
 * @test
 * @summary com.sun.xml.internal.ws.assembler.dev.ClientTubelineAssemblyContext conformance: reflection (kind, superclass, interfaces, methods).
 * @run main ClientTubelineAssemblyContextTest
 */

import java.lang.reflect.Modifier;


public class ClientTubelineAssemblyContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.assembler.dev.ClientTubelineAssemblyContext", false, ClientTubelineAssemblyContextTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.assembler.dev.ClientTubelineAssemblyContext"), "name");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interface superclass null");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.sun.xml.internal.ws.assembler.dev.TubelineAssemblyContext"), "implements com.sun.xml.internal.ws.assembler.dev.TubelineAssemblyContext");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("getAddress"), "method getAddress");
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getCodec"), "method getCodec");
        check(mn.contains("getContainer"), "method getContainer");
        check(mn.contains("getPolicyMap"), "method getPolicyMap");
        check(mn.contains("getPortInfo"), "method getPortInfo");
        check(mn.contains("getSEIModel"), "method getSEIModel");
        check(mn.contains("getService"), "method getService");
        check(mn.contains("getWrappedContext"), "method getWrappedContext");
        check(mn.contains("getWsdlPort"), "method getWsdlPort");
        check(mn.contains("isPolicyAvailable"), "method isPolicyAvailable");
        check(mn.contains("setCodec"), "method setCodec");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
