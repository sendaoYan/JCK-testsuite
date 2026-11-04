/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.algorithms.JCEMapper conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.algorithms:+open
 * @run main JCEMapperTest
 */
import java.lang.reflect.*;
import java.util.*;
public class JCEMapperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.algorithms.JCEMapper", false, JCEMapperTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.algorithms.JCEMapper"), "name");
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
        check(methods.contains("getAlgorithmClassFromURI"), "method:getAlgorithmClassFromURI");
        check(methods.contains("getIVLengthFromURI"), "method:getIVLengthFromURI");
        check(methods.contains("getJCEKeyAlgorithmFromURI"), "method:getJCEKeyAlgorithmFromURI");
        check(methods.contains("getJCEProviderFromURI"), "method:getJCEProviderFromURI");
        check(methods.contains("getKeyLengthFromURI"), "method:getKeyLengthFromURI");
        check(methods.contains("getProviderId"), "method:getProviderId");
        check(methods.contains("register"), "method:register");
        check(methods.contains("registerDefaultAlgorithms"), "method:registerDefaultAlgorithms");
        check(methods.contains("setProviderId"), "method:setProviderId");
        check(methods.contains("setThreadSpecificProviderName"), "method:setThreadSpecificProviderName");
        check(methods.contains("translateURItoJCEID"), "method:translateURItoJCEID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
