/*
 * @test
 * @summary com.sun.corba.se.impl.logging.POASystemException conformance: type shape via reflection (headless-safe).
 * @run main POASystemExceptionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class POASystemExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.logging.POASystemException", false, POASystemExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.logging.LogWrapperBase"), "superclass com.sun.corba.se.spi.logging.LogWrapperBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 72, "public declared method count");
        check(mn.contains("adapterActivatorFailed"), "method adapterActivatorFailed");
        check(mn.contains("adapterActivatorNonexistent"), "method adapterActivatorNonexistent");
        check(mn.contains("adapterDestroyed"), "method adapterDestroyed");
        check(mn.contains("addPoaInactive"), "method addPoaInactive");
        check(mn.contains("aomEntryDecZero"), "method aomEntryDecZero");
        check(mn.contains("badLocalreplystatus"), "method badLocalreplystatus");
        check(mn.contains("badRepositoryId"), "method badRepositoryId");
        check(mn.contains("badServant"), "method badServant");
        check(mn.contains("badSkeleton"), "method badSkeleton");
        check(mn.contains("badTransactionContext"), "method badTransactionContext");
        check(mn.contains("bootstrapError"), "method bootstrapError");
        check(mn.contains("cantCloneTemplate"), "method cantCloneTemplate");
        check(mn.contains("cantResolveRootPoa"), "method cantResolveRootPoa");
        check(mn.contains("defaultPoaNotPoaimpl"), "method defaultPoaNotPoaimpl");
        check(mn.contains("destroyDeadlock"), "method destroyDeadlock");
        check(mn.contains("duplicateOrbVersionSc"), "method duplicateOrbVersionSc");
        check(mn.contains("findpoaError"), "method findpoaError");
        check(mn.contains("get"), "method get");
        check(mn.contains("illegalForwardRequest"), "method illegalForwardRequest");
        check(mn.contains("illegalPoaStateTrans"), "method illegalPoaStateTrans");
        check(mn.contains("incarnateReturnedNull"), "method incarnateReturnedNull");
        check(mn.contains("invokesetup"), "method invokesetup");
        check(mn.contains("jtsInitError"), "method jtsInitError");
        check(mn.contains("localServantLookup"), "method localServantLookup");
        check(mn.contains("makeFactoryNotPoa"), "method makeFactoryNotPoa");
        check(mn.contains("methodNotImplemented"), "method methodNotImplemented");
        check(mn.contains("noContext"), "method noContext");
        check(mn.contains("noProfilesInIor"), "method noProfilesInIor");
        check(mn.contains("nullServant"), "method nullServant");
        check(mn.contains("orbdError"), "method orbdError");
        check(mn.contains("otshookexception"), "method otshookexception");
        check(mn.contains("persistentServeridNotSet"), "method persistentServeridNotSet");
        check(mn.contains("persistentServerportError"), "method persistentServerportError");
        check(mn.contains("persistentServerportNotSet"), "method persistentServerportNotSet");
        check(mn.contains("pmfCreateNonRetain"), "method pmfCreateNonRetain");
        check(mn.contains("pmfCreateRetain"), "method pmfCreateRetain");
        check(mn.contains("poaBadServantManager"), "method poaBadServantManager");
        check(mn.contains("poaDiscarding"), "method poaDiscarding");
        check(mn.contains("poaInactive"), "method poaInactive");
        check(mn.contains("poaInternalGetServantError"), "method poaInternalGetServantError");
        check(mn.contains("poaLookupError"), "method poaLookupError");
        check(mn.contains("poaNoDefaultServant"), "method poaNoDefaultServant");
        check(mn.contains("poaNoServantManager"), "method poaNoServantManager");
        check(mn.contains("poaNotFound"), "method poaNotFound");
        check(mn.contains("poaServantActivatorLookupFailed"), "method poaServantActivatorLookupFailed");
        check(mn.contains("poaServantLocatorLookupFailed"), "method poaServantLocatorLookupFailed");
        check(mn.contains("poaServantNotUnique"), "method poaServantNotUnique");
        check(mn.contains("poaUnknownPolicy"), "method poaUnknownPolicy");
        check(mn.contains("poaWrongPolicy"), "method poaWrongPolicy");
        check(mn.contains("poacurrentNullField"), "method poacurrentNullField");
        check(mn.contains("poacurrentUnbalancedStack"), "method poacurrentUnbalancedStack");
        check(mn.contains("policyMediatorBadPolicyInFactory"), "method policyMediatorBadPolicyInFactory");
        check(mn.contains("preinvokeCloneError"), "method preinvokeCloneError");
        check(mn.contains("preinvokePoaDestroyed"), "method preinvokePoaDestroyed");
        check(mn.contains("servantDispatch"), "method servantDispatch");
        check(mn.contains("servantLookup"), "method servantLookup");
        check(mn.contains("servantManagerAlreadySet"), "method servantManagerAlreadySet");
        check(mn.contains("servantManagerBadType"), "method servantManagerBadType");
        check(mn.contains("servantMustBeLocal"), "method servantMustBeLocal");
        check(mn.contains("servantOrb"), "method servantOrb");
        check(mn.contains("servantToIdOaa"), "method servantToIdOaa");
        check(mn.contains("servantToIdSaa"), "method servantToIdSaa");
        check(mn.contains("servantToIdWp"), "method servantToIdWp");
        check(mn.contains("singleThreadNotSupported"), "method singleThreadNotSupported");
        check(mn.contains("thisObjectServantNotActive"), "method thisObjectServantNotActive");
        check(mn.contains("thisObjectWrongPolicy"), "method thisObjectWrongPolicy");
        check(mn.contains("unexpectedException"), "method unexpectedException");
        check(mn.contains("unknownLocalinvocationError"), "method unknownLocalinvocationError");
        check(mn.contains("unknownServerException"), "method unknownServerException");
        check(mn.contains("unknownServerappException"), "method unknownServerappException");
        check(mn.contains("wrongClientsc"), "method wrongClientsc");
        check(mn.contains("wrongPoliciesForThisObject"), "method wrongPoliciesForThisObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
