/*
 * @test
 * @summary sun.security.ssl.SSLSocketImpl conformance: internal package reflection.
 * @modules java.base/sun.security.ssl:+open
 * @run main SSLSocketImplTest
 */
import java.lang.reflect.*;
import java.util.*;

public class SSLSocketImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.ssl.SSLSocketImpl", false, SSLSocketImplTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.ssl.SSLSocketImpl".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.security.ssl.BaseSSLSocketImpl".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<>(Arrays.asList("sun.security.ssl.SSLTransport"))), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("addHandshakeCompletedListener", "bind", "close", "connect", "getApplicationProtocol", "getEnableSessionCreation", "getEnabledCipherSuites", "getEnabledProtocols", "getHandshakeApplicationProtocol", "getHandshakeApplicationProtocolSelector", "getHandshakeSession", "getInputStream", "getLocalSocketAddress", "getNeedClientAuth", "getOption", "getOutputStream", "getPeerHost", "getPeerPort", "getRemoteSocketAddress", "getSSLParameters", "getSession", "getSupportedCipherSuites", "getSupportedProtocols", "getUseClientMode", "getWantClientAuth", "isClosed", "isInputShutdown", "isOutputShutdown", "removeHandshakeCompletedListener", "setEnableSessionCreation", "setEnabledCipherSuites", "setEnabledProtocols", "setHandshakeApplicationProtocolSelector", "setHost", "setNeedClientAuth", "setOption", "setPerformancePreferences", "setSSLParameters", "setSoTimeout", "setUseClientMode", "setWantClientAuth", "shutdown", "shutdownInput", "shutdownOutput", "startHandshake", "supportedOptions", "toString", "useDelegatedTask"))), "methods");
        check(fieldNames(c).equals(new TreeSet<String>()), "fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static Set<String> interfaceNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mm = m.getModifiers();
            if (Modifier.isPublic(mm) || Modifier.isProtected(mm)) s.add(m.getName());
        }
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Field fld : c.getDeclaredFields()) {
            if (fld.isEnumConstant()) continue;
            int fm = fld.getModifiers();
            if (Modifier.isPublic(fm) || Modifier.isProtected(fm)) s.add(fld.getName());
        }
        return s;
    }
    static Set<String> enumConstantNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Field fld : c.getDeclaredFields()) {
            if (fld.isEnumConstant()) s.add(fld.getName());
        }
        return s;
    }
}
