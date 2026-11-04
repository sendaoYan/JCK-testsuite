/*
 * @test
 * @summary javax.net.ssl.ManagerFactoryParameters conformance: marker interface contract.
 * @run main ManagerFactoryParametersTest
 */
import java.lang.reflect.*;
public class ManagerFactoryParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.ManagerFactoryParameters");
        check(c.isInterface() && Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getDeclaredMethods().length == 0, "marker interface: no declared methods");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        javax.net.ssl.ManagerFactoryParameters p = new javax.net.ssl.ManagerFactoryParameters() { };
        check(p instanceof javax.net.ssl.ManagerFactoryParameters, "anonymous implementation assignable");
        // KeyStoreBuilderParameters is the standard implementation of this marker
        java.security.KeyStore.Builder b = java.security.KeyStore.Builder.newInstance(
                java.security.KeyStore.getDefaultType(), null,
                new java.security.KeyStore.PasswordProtection(new char[0]));
        javax.net.ssl.KeyStoreBuilderParameters ksb =
                new javax.net.ssl.KeyStoreBuilderParameters(java.util.Collections.singletonList(b));
        check(ksb instanceof javax.net.ssl.ManagerFactoryParameters,
                "KeyStoreBuilderParameters implements ManagerFactoryParameters");
        check(javax.net.ssl.KeyStoreBuilderParameters.class.getInterfaces().length == 1
                && javax.net.ssl.KeyStoreBuilderParameters.class.getInterfaces()[0].getName()
                        .equals("javax.net.ssl.ManagerFactoryParameters"),
                "KeyStoreBuilderParameters directly implements the marker");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
