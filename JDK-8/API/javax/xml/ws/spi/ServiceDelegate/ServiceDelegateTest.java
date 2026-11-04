/*
 * @test
 * @summary javax.xml.ws.spi.ServiceDelegate conformance: abstract class hierarchy and methods.
 * @run main ServiceDelegateTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ServiceDelegateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.spi.ServiceDelegate");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getPort"), "has method getPort");
        check(names.contains("addPort"), "has method addPort");
        check(names.contains("createDispatch"), "has method createDispatch");
        check(names.contains("getServiceName"), "has method getServiceName");
        check(names.contains("getPorts"), "has method getPorts");
        check(names.contains("getWSDLDocumentLocation"), "has method getWSDLDocumentLocation");
        check(names.contains("getHandlerResolver"), "has method getHandlerResolver");
        check(names.contains("setHandlerResolver"), "has method setHandlerResolver");
        check(names.contains("getExecutor"), "has method getExecutor");
        check(names.contains("setExecutor"), "has method setExecutor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
