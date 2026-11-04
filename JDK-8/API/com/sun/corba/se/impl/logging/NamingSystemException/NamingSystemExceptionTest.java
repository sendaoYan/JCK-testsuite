/*
 * @test
 * @summary com.sun.corba.se.impl.logging.NamingSystemException conformance: type shape via reflection (headless-safe).
 * @run main NamingSystemExceptionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class NamingSystemExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.logging.NamingSystemException", false, NamingSystemExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.logging.LogWrapperBase"), "superclass com.sun.corba.se.spi.logging.LogWrapperBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 24, "public declared method count");
        check(mn.contains("bindFailure"), "method bindFailure");
        check(mn.contains("bindUpdateContextFailed"), "method bindUpdateContextFailed");
        check(mn.contains("get"), "method get");
        check(mn.contains("insBadAddress"), "method insBadAddress");
        check(mn.contains("insBadSchemeName"), "method insBadSchemeName");
        check(mn.contains("insBadSchemeSpecificPart"), "method insBadSchemeSpecificPart");
        check(mn.contains("insOther"), "method insOther");
        check(mn.contains("namingCtxBadBindingtype"), "method namingCtxBadBindingtype");
        check(mn.contains("namingCtxBindingIteratorCreate"), "method namingCtxBindingIteratorCreate");
        check(mn.contains("namingCtxRebindAlreadyBound"), "method namingCtxRebindAlreadyBound");
        check(mn.contains("namingCtxRebindctxAlreadyBound"), "method namingCtxRebindctxAlreadyBound");
        check(mn.contains("namingCtxResolveCannotNarrowToCtx"), "method namingCtxResolveCannotNarrowToCtx");
        check(mn.contains("objectIsNull"), "method objectIsNull");
        check(mn.contains("resolveConversionFailure"), "method resolveConversionFailure");
        check(mn.contains("resolveFailure"), "method resolveFailure");
        check(mn.contains("transNcBindAlreadyBound"), "method transNcBindAlreadyBound");
        check(mn.contains("transNcDestroyGotExc"), "method transNcDestroyGotExc");
        check(mn.contains("transNcListGotExc"), "method transNcListGotExc");
        check(mn.contains("transNcNewctxGotExc"), "method transNcNewctxGotExc");
        check(mn.contains("transNsCannotCreateInitialNc"), "method transNsCannotCreateInitialNc");
        check(mn.contains("transNsCannotCreateInitialNcSys"), "method transNsCannotCreateInitialNcSys");
        check(mn.contains("transientNameServerBadHost"), "method transientNameServerBadHost");
        check(mn.contains("transientNameServerBadPort"), "method transientNameServerBadPort");
        check(mn.contains("unbindFailure"), "method unbindFailure");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
