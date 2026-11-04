/*
 * @test
 * @summary jdk.internal.net.http.common.HttpBodySubscriberWrapper conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main HttpBodySubscriberWrapperTest
 */
import java.lang.reflect.*;
import java.util.*;
public class HttpBodySubscriberWrapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.HttpBodySubscriberWrapper", false, HttpBodySubscriberWrapperTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.HttpBodySubscriberWrapper"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.ResponseSubscribers$TrustedSubscriber"), "iface:jdk.internal.net.http.ResponseSubscribers$TrustedSubscriber");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("cancelled"), "method:cancelled");
        check(methods.contains("complete"), "method:complete");
        check(methods.contains("completed"), "method:completed");
        check(methods.contains("getBody"), "method:getBody");
        check(methods.contains("logComplete"), "method:logComplete");
        check(methods.contains("needsExecutor"), "method:needsExecutor");
        check(methods.contains("onCancel"), "method:onCancel");
        check(methods.contains("onComplete"), "method:onComplete");
        check(methods.contains("onError"), "method:onError");
        check(methods.contains("onNext"), "method:onNext");
        check(methods.contains("onSubscribe"), "method:onSubscribe");
        check(methods.contains("onTermination"), "method:onTermination");
        check(methods.contains("register"), "method:register");
        check(methods.contains("registered"), "method:registered");
        check(methods.contains("subscribed"), "method:subscribed");
        check(methods.contains("tryRegister"), "method:tryRegister");
        check(methods.contains("tryUnregister"), "method:tryUnregister");
        check(methods.contains("unregister"), "method:unregister");
        check(methods.contains("unregistered"), "method:unregistered");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("COMPARE_BY_ID"), "field:COMPARE_BY_ID");
        check(fields.contains("NOP"), "field:NOP");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
