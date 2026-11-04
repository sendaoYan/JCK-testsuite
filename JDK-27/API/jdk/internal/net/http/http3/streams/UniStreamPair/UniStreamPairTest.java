/*
 * @test
 * @summary jdk.internal.net.http.http3.streams.UniStreamPair conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3.streams:+open
 * @run main UniStreamPairTest
 */
import java.lang.reflect.*;
import java.util.*;
public class UniStreamPairTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.streams.UniStreamPair", false, UniStreamPairTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.streams.UniStreamPair"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("futureReceiverStream"), "method:futureReceiverStream");
        check(methods.contains("futureReceiverStreamReader"), "method:futureReceiverStreamReader");
        check(methods.contains("futureSenderStream"), "method:futureSenderStream");
        check(methods.contains("futureSenderStreamWriter"), "method:futureSenderStreamWriter");
        check(methods.contains("localStreamType"), "method:localStreamType");
        check(methods.contains("localWriteScheduler"), "method:localWriteScheduler");
        check(methods.contains("localWriter"), "method:localWriter");
        check(methods.contains("remoteStreamType"), "method:remoteStreamType");
        check(methods.contains("stopSchedulers"), "method:stopSchedulers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
