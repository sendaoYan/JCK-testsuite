/*
 * @test
 * @summary jdk.internal.net.http.common.ImmutableSSLSession conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main ImmutableSSLSessionTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ImmutableSSLSessionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.ImmutableSSLSession", false, ImmutableSSLSessionTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.ImmutableSSLSession"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("javax.net.ssl.SSLSession"), "iface:javax.net.ssl.SSLSession");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getApplicationBufferSize"), "method:getApplicationBufferSize");
        check(methods.contains("getCipherSuite"), "method:getCipherSuite");
        check(methods.contains("getCreationTime"), "method:getCreationTime");
        check(methods.contains("getId"), "method:getId");
        check(methods.contains("getLastAccessedTime"), "method:getLastAccessedTime");
        check(methods.contains("getLocalCertificates"), "method:getLocalCertificates");
        check(methods.contains("getLocalPrincipal"), "method:getLocalPrincipal");
        check(methods.contains("getPacketBufferSize"), "method:getPacketBufferSize");
        check(methods.contains("getPeerCertificates"), "method:getPeerCertificates");
        check(methods.contains("getPeerHost"), "method:getPeerHost");
        check(methods.contains("getPeerPort"), "method:getPeerPort");
        check(methods.contains("getPeerPrincipal"), "method:getPeerPrincipal");
        check(methods.contains("getProtocol"), "method:getProtocol");
        check(methods.contains("getSessionContext"), "method:getSessionContext");
        check(methods.contains("getValue"), "method:getValue");
        check(methods.contains("getValueNames"), "method:getValueNames");
        check(methods.contains("invalidate"), "method:invalidate");
        check(methods.contains("isValid"), "method:isValid");
        check(methods.contains("putValue"), "method:putValue");
        check(methods.contains("removeValue"), "method:removeValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
