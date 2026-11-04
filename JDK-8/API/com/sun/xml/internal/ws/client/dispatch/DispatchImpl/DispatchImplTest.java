/*
 * @test
 * @summary com.sun.xml.internal.ws.client.dispatch.DispatchImpl conformance: reflection (kind, superclass, interfaces, methods).
 * @run main DispatchImplTest
 */

import java.lang.reflect.Modifier;


public class DispatchImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.client.dispatch.DispatchImpl", false, DispatchImplTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.client.dispatch.DispatchImpl"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.sun.xml.internal.ws.client.Stub"), "superclass com.sun.xml.internal.ws.client.Stub");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.ws.Dispatch"), "implements javax.xml.ws.Dispatch");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("checkValidDataSourceDispatch"), "method checkValidDataSourceDispatch");
        check(mn.contains("checkValidSOAPMessageDispatch"), "method checkValidSOAPMessageDispatch");
        check(mn.contains("createSourceDispatch"), "method createSourceDispatch");
        check(mn.contains("doInvoke"), "method doInvoke");
        check(mn.contains("getPortName"), "method getPortName");
        check(mn.contains("invoke"), "method invoke");
        check(mn.contains("invokeAsync"), "method invokeAsync");
        check(mn.contains("invokeOneWay"), "method invokeOneWay");
        check(mn.contains("resolveURI"), "method resolveURI");
        check(mn.contains("setOutboundAttachments"), "method setOutboundAttachments");
        check(mn.contains("setOutboundHeaders"), "method setOutboundHeaders");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
