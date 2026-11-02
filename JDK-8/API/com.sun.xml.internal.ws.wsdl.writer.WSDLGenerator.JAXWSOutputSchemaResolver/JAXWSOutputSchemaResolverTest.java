/*
 * @test
 * @summary com.sun.xml.internal.ws.wsdl.writer.WSDLGenerator.JAXWSOutputSchemaResolver conformance: reflection (kind, superclass, interfaces, methods).
 * @run main JAXWSOutputSchemaResolverTest
 */

import java.lang.reflect.Modifier;


public class JAXWSOutputSchemaResolverTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.wsdl.writer.WSDLGenerator$JAXWSOutputSchemaResolver", false, JAXWSOutputSchemaResolverTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.wsdl.writer.WSDLGenerator$JAXWSOutputSchemaResolver"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(c.getSuperclass().getName().equals("javax.xml.bind.SchemaOutputResolver"), "superclass javax.xml.bind.SchemaOutputResolver");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("createOutput"), "method createOutput");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
