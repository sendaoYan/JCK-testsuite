/*
 * @test
 * @summary jdk.internal.net.http.common.ImmutableExtendedSSLSession conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main ImmutableExtendedSSLSessionTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ImmutableExtendedSSLSessionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.ImmutableExtendedSSLSession", false, ImmutableExtendedSSLSessionTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.ImmutableExtendedSSLSession"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("javax.net.ssl.ExtendedSSLSession"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
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
        check(methods.contains("getLocalSupportedSignatureAlgorithms"), "method:getLocalSupportedSignatureAlgorithms");
        check(methods.contains("getPacketBufferSize"), "method:getPacketBufferSize");
        check(methods.contains("getPeerCertificates"), "method:getPeerCertificates");
        check(methods.contains("getPeerHost"), "method:getPeerHost");
        check(methods.contains("getPeerPort"), "method:getPeerPort");
        check(methods.contains("getPeerPrincipal"), "method:getPeerPrincipal");
        check(methods.contains("getPeerSupportedSignatureAlgorithms"), "method:getPeerSupportedSignatureAlgorithms");
        check(methods.contains("getProtocol"), "method:getProtocol");
        check(methods.contains("getRequestedServerNames"), "method:getRequestedServerNames");
        check(methods.contains("getSessionContext"), "method:getSessionContext");
        check(methods.contains("getStatusResponses"), "method:getStatusResponses");
        check(methods.contains("getValue"), "method:getValue");
        check(methods.contains("getValueNames"), "method:getValueNames");
        check(methods.contains("invalidate"), "method:invalidate");
        check(methods.contains("isValid"), "method:isValid");
        check(methods.contains("putValue"), "method:putValue");
        check(methods.contains("removeValue"), "method:removeValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
