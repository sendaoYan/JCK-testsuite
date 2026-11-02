/*
 * @test
 * @summary com.sun.corba.se.impl.logging.InterceptorsSystemException conformance: type shape via reflection (headless-safe).
 * @run main InterceptorsSystemExceptionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class InterceptorsSystemExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.logging.InterceptorsSystemException", false, InterceptorsSystemExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.logging.LogWrapperBase"), "superclass com.sun.corba.se.spi.logging.LogWrapperBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 32, "public declared method count");
        check(mn.contains("badState1"), "method badState1");
        check(mn.contains("badState2"), "method badState2");
        check(mn.contains("cantPopOnlyCurrent2"), "method cantPopOnlyCurrent2");
        check(mn.contains("cantPopOnlyPicurrent"), "method cantPopOnlyPicurrent");
        check(mn.contains("clientInfoStackNull"), "method clientInfoStackNull");
        check(mn.contains("delegateNotClientsub"), "method delegateNotClientsub");
        check(mn.contains("exceptionInArguments"), "method exceptionInArguments");
        check(mn.contains("exceptionInContexts"), "method exceptionInContexts");
        check(mn.contains("exceptionInExceptions"), "method exceptionInExceptions");
        check(mn.contains("exceptionInvalid"), "method exceptionInvalid");
        check(mn.contains("exceptionUnavailable"), "method exceptionUnavailable");
        check(mn.contains("exceptionWasNull"), "method exceptionWasNull");
        check(mn.contains("exceptionWasNull2"), "method exceptionWasNull2");
        check(mn.contains("get"), "method get");
        check(mn.contains("interceptorListLocked"), "method interceptorListLocked");
        check(mn.contains("ioexceptionDuringCancelRequest"), "method ioexceptionDuringCancelRequest");
        check(mn.contains("markAndResetFailed"), "method markAndResetFailed");
        check(mn.contains("nameNull"), "method nameNull");
        check(mn.contains("objectHasNoDelegate"), "method objectHasNoDelegate");
        check(mn.contains("objectNotObjectimpl"), "method objectNotObjectimpl");
        check(mn.contains("orbinitinfoInvalid"), "method orbinitinfoInvalid");
        check(mn.contains("piDiiResultIsNull"), "method piDiiResultIsNull");
        check(mn.contains("piDsiResultIsNull"), "method piDsiResultIsNull");
        check(mn.contains("piOrbNotPolicyBased"), "method piOrbNotPolicyBased");
        check(mn.contains("replyStatusNotInit"), "method replyStatusNotInit");
        check(mn.contains("rirInvalidPreInit"), "method rirInvalidPreInit");
        check(mn.contains("servantInvalid"), "method servantInvalid");
        check(mn.contains("serverInfoStackNull"), "method serverInfoStackNull");
        check(mn.contains("slotTableInvariant"), "method slotTableInvariant");
        check(mn.contains("sortSizeMismatch"), "method sortSizeMismatch");
        check(mn.contains("typeOutOfRange"), "method typeOutOfRange");
        check(mn.contains("unknownRequestInvoke"), "method unknownRequestInvoke");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
