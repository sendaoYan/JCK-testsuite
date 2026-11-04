/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicTransportParameters conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicTransportParametersTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicTransportParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicTransportParameters", false, QuicTransportParametersTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicTransportParameters"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("buildVersionInformation"), "method:buildVersionInformation");
        check(methods.contains("decode"), "method:decode");
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("getBooleanParameter"), "method:getBooleanParameter");
        check(methods.contains("getIntParameter"), "method:getIntParameter");
        check(methods.contains("getParameter"), "method:getParameter");
        check(methods.contains("getPreferredAddress"), "method:getPreferredAddress");
        check(methods.contains("getPreferredConnectionId"), "method:getPreferredConnectionId");
        check(methods.contains("getPreferredStatelessResetToken"), "method:getPreferredStatelessResetToken");
        check(methods.contains("getVersionInformationParameter"), "method:getVersionInformationParameter");
        check(methods.contains("isPresent"), "method:isPresent");
        check(methods.contains("matches"), "method:matches");
        check(methods.contains("setBooleanParameter"), "method:setBooleanParameter");
        check(methods.contains("setIntParameter"), "method:setIntParameter");
        check(methods.contains("setParameter"), "method:setParameter");
        check(methods.contains("setPreferredAddressParameter"), "method:setPreferredAddressParameter");
        check(methods.contains("setVersionInformationParameter"), "method:setVersionInformationParameter");
        check(methods.contains("size"), "method:size");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("toStringWithValues"), "method:toStringWithValues");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
