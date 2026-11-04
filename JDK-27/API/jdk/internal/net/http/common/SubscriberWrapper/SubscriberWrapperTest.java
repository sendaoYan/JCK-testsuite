/*
 * @test
 * @summary jdk.internal.net.http.common.SubscriberWrapper conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main SubscriberWrapperTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SubscriberWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.SubscriberWrapper", false, SubscriberWrapperTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.SubscriberWrapper"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.io.Closeable"), "iface:java.io.Closeable");
        check(ifaces.contains("java.util.concurrent.Flow$Processor"), "iface:java.util.concurrent.Flow$Processor");
        check(ifaces.contains("jdk.internal.net.http.common.FlowTube$TubeSubscriber"), "iface:jdk.internal.net.http.common.FlowTube$TubeSubscriber");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addData"), "method:addData");
        check(methods.contains("close"), "method:close");
        check(methods.contains("closing"), "method:closing");
        check(methods.contains("completion"), "method:completion");
        check(methods.contains("dbgString"), "method:dbgString");
        check(methods.contains("enterScheduling"), "method:enterScheduling");
        check(methods.contains("errorCommon"), "method:errorCommon");
        check(methods.contains("incoming"), "method:incoming");
        check(methods.contains("initialUpstreamDemand"), "method:initialUpstreamDemand");
        check(methods.contains("onComplete"), "method:onComplete");
        check(methods.contains("onError"), "method:onError");
        check(methods.contains("onNext"), "method:onNext");
        check(methods.contains("onSubscribe"), "method:onSubscribe");
        check(methods.contains("outgoing"), "method:outgoing");
        check(methods.contains("requestMore"), "method:requestMore");
        check(methods.contains("resetDownstreamDemand"), "method:resetDownstreamDemand");
        check(methods.contains("signalScheduling"), "method:signalScheduling");
        check(methods.contains("subscribe"), "method:subscribe");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("upstreamWindow"), "method:upstreamWindow");
        check(methods.contains("upstreamWindowUpdate"), "method:upstreamWindowUpdate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
