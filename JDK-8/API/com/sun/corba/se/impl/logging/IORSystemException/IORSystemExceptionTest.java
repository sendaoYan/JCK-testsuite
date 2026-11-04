/*
 * @test
 * @summary com.sun.corba.se.impl.logging.IORSystemException conformance: type shape via reflection (headless-safe).
 * @run main IORSystemExceptionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IORSystemExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.logging.IORSystemException", false, IORSystemExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.logging.LogWrapperBase"), "superclass com.sun.corba.se.spi.logging.LogWrapperBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 16, "public declared method count");
        check(mn.contains("adapterIdNotAvailable"), "method adapterIdNotAvailable");
        check(mn.contains("badIiopAddressPort"), "method badIiopAddressPort");
        check(mn.contains("badMagic"), "method badMagic");
        check(mn.contains("badOidInIorTemplateList"), "method badOidInIorTemplateList");
        check(mn.contains("get"), "method get");
        check(mn.contains("getLocalServantFailure"), "method getLocalServantFailure");
        check(mn.contains("invalidJdk131PatchLevel"), "method invalidJdk131PatchLevel");
        check(mn.contains("invalidTaggedProfile"), "method invalidTaggedProfile");
        check(mn.contains("iorMustHaveIiopProfile"), "method iorMustHaveIiopProfile");
        check(mn.contains("nullPoa"), "method nullPoa");
        check(mn.contains("objectAdapterIdNotAvailable"), "method objectAdapterIdNotAvailable");
        check(mn.contains("orbIdNotAvailable"), "method orbIdNotAvailable");
        check(mn.contains("ortNotInitialized"), "method ortNotInitialized");
        check(mn.contains("serverIdNotAvailable"), "method serverIdNotAvailable");
        check(mn.contains("stringifyWriteError"), "method stringifyWriteError");
        check(mn.contains("taggedProfileTemplateFactoryNotFound"), "method taggedProfileTemplateFactoryNotFound");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
