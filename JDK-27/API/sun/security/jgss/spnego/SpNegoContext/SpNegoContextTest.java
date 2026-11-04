/*
 * @test
 * @summary sun.security.jgss.spnego.SpNegoContext conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.spnego:+open
 * @run main SpNegoContextTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SpNegoContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.spnego.SpNegoContext", false, SpNegoContextTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.spnego.SpNegoContext"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("sun.security.jgss.spi.GSSContextSpi"), "iface:sun.security.jgss.spi.GSSContextSpi");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("acceptSecContext"), "method:acceptSecContext");
        check(methods.contains("dispose"), "method:dispose");
        check(methods.contains("export"), "method:export");
        check(methods.contains("getAnonymityState"), "method:getAnonymityState");
        check(methods.contains("getConfState"), "method:getConfState");
        check(methods.contains("getCredDelegState"), "method:getCredDelegState");
        check(methods.contains("getDelegCred"), "method:getDelegCred");
        check(methods.contains("getDelegPolicyState"), "method:getDelegPolicyState");
        check(methods.contains("getIntegState"), "method:getIntegState");
        check(methods.contains("getLifetime"), "method:getLifetime");
        check(methods.contains("getMIC"), "method:getMIC");
        check(methods.contains("getMech"), "method:getMech");
        check(methods.contains("getMutualAuthState"), "method:getMutualAuthState");
        check(methods.contains("getNegotiatedMech"), "method:getNegotiatedMech");
        check(methods.contains("getProvider"), "method:getProvider");
        check(methods.contains("getReplayDetState"), "method:getReplayDetState");
        check(methods.contains("getSequenceDetState"), "method:getSequenceDetState");
        check(methods.contains("getSrcName"), "method:getSrcName");
        check(methods.contains("getTargName"), "method:getTargName");
        check(methods.contains("getWrapSizeLimit"), "method:getWrapSizeLimit");
        check(methods.contains("initSecContext"), "method:initSecContext");
        check(methods.contains("inquireSecContext"), "method:inquireSecContext");
        check(methods.contains("isEstablished"), "method:isEstablished");
        check(methods.contains("isInitiator"), "method:isInitiator");
        check(methods.contains("isMechContextEstablished"), "method:isMechContextEstablished");
        check(methods.contains("isProtReady"), "method:isProtReady");
        check(methods.contains("isTransferable"), "method:isTransferable");
        check(methods.contains("requestAnonymity"), "method:requestAnonymity");
        check(methods.contains("requestConf"), "method:requestConf");
        check(methods.contains("requestCredDeleg"), "method:requestCredDeleg");
        check(methods.contains("requestDelegPolicy"), "method:requestDelegPolicy");
        check(methods.contains("requestInteg"), "method:requestInteg");
        check(methods.contains("requestLifetime"), "method:requestLifetime");
        check(methods.contains("requestMutualAuth"), "method:requestMutualAuth");
        check(methods.contains("requestReplayDet"), "method:requestReplayDet");
        check(methods.contains("requestSequenceDet"), "method:requestSequenceDet");
        check(methods.contains("setChannelBinding"), "method:setChannelBinding");
        check(methods.contains("unwrap"), "method:unwrap");
        check(methods.contains("verifyMIC"), "method:verifyMIC");
        check(methods.contains("wrap"), "method:wrap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
