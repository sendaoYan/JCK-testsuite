/*
 * @test
 * @summary jdk.internal.net.http.common.FlowTube.AbstractTubeSubscriber conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main AbstractTubeSubscriberTest
 */
import java.lang.reflect.*;
import java.util.*;
public class AbstractTubeSubscriberTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.FlowTube$AbstractTubeSubscriber", false, AbstractTubeSubscriberTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.FlowTube$AbstractTubeSubscriber"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.common.FlowTube$TubeSubscriber"), "iface:jdk.internal.net.http.common.FlowTube$TubeSubscriber");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
