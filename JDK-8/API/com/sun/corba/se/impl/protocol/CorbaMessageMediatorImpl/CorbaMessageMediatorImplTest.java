/*
 * @test
 * @summary com.sun.corba.se.impl.protocol.CorbaMessageMediatorImpl conformance: type shape via reflection (headless-safe).
 * @run main CorbaMessageMediatorImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CorbaMessageMediatorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.protocol.CorbaMessageMediatorImpl", false, CorbaMessageMediatorImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.protocol.giopmsgheaders.MessageHandler"), "interface com.sun.corba.se.impl.protocol.giopmsgheaders.MessageHandler");
        check(in.contains("com.sun.corba.se.spi.protocol.CorbaMessageMediator"), "interface com.sun.corba.se.spi.protocol.CorbaMessageMediator");
        check(in.contains("com.sun.corba.se.spi.protocol.CorbaProtocolHandler"), "interface com.sun.corba.se.spi.protocol.CorbaProtocolHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 65, "public declared method count");
        check(mn.contains("createExceptionReply"), "method createExceptionReply");
        check(mn.contains("createLocationForward"), "method createLocationForward");
        check(mn.contains("createReply"), "method createReply");
        check(mn.contains("createResponse"), "method createResponse");
        check(mn.contains("createSystemExceptionResponse"), "method createSystemExceptionResponse");
        check(mn.contains("createUnknownExceptionResponse"), "method createUnknownExceptionResponse");
        check(mn.contains("createUserExceptionResponse"), "method createUserExceptionResponse");
        check(mn.contains("executePIInResponseConstructor"), "method executePIInResponseConstructor");
        check(mn.contains("executeRemoveThreadInfoInResponseConstructor"), "method executeRemoveThreadInfoInResponseConstructor");
        check(mn.contains("executeReturnServantInResponseConstructor"), "method executeReturnServantInResponseConstructor");
        check(mn.contains("finishSendingRequest"), "method finishSendingRequest");
        check(mn.contains("getAddrDisposition"), "method getAddrDisposition");
        check(mn.contains("getAddrDispositionReply"), "method getAddrDispositionReply");
        check(mn.contains("getBroker"), "method getBroker");
        check(mn.contains("getConnection"), "method getConnection");
        check(mn.contains("getContactInfo"), "method getContactInfo");
        check(mn.contains("getDispatchBuffer"), "method getDispatchBuffer");
        check(mn.contains("getDispatchHeader"), "method getDispatchHeader");
        check(mn.contains("getEncodingVersion"), "method getEncodingVersion");
        check(mn.contains("getForwardedIOR"), "method getForwardedIOR");
        check(mn.contains("getGIOPVersion"), "method getGIOPVersion");
        check(mn.contains("getInputObject"), "method getInputObject");
        check(mn.contains("getLocateReplyHeader"), "method getLocateReplyHeader");
        check(mn.contains("getObjectKey"), "method getObjectKey");
        check(mn.contains("getOperationName"), "method getOperationName");
        check(mn.contains("getOutputObject"), "method getOutputObject");
        check(mn.contains("getProtocolHandler"), "method getProtocolHandler");
        check(mn.contains("getReplyHeader"), "method getReplyHeader");
        check(mn.contains("getReplyServiceContexts"), "method getReplyServiceContexts");
        check(mn.contains("getRequestHeader"), "method getRequestHeader");
        check(mn.contains("getRequestId"), "method getRequestId");
        check(mn.contains("getRequestIdInteger"), "method getRequestIdInteger");
        check(mn.contains("getRequestServiceContexts"), "method getRequestServiceContexts");
        check(mn.contains("getStreamFormatVersion"), "method getStreamFormatVersion");
        check(mn.contains("getStreamFormatVersionForReply"), "method getStreamFormatVersionForReply");
        check(mn.contains("getSystemExceptionReply"), "method getSystemExceptionReply");
        check(mn.contains("getThreadPoolToUse"), "method getThreadPoolToUse");
        check(mn.contains("handleDIIReply"), "method handleDIIReply");
        check(mn.contains("handleInput"), "method handleInput");
        check(mn.contains("handleRequest"), "method handleRequest");
        check(mn.contains("handleThrowableDuringServerDispatch"), "method handleThrowableDuringServerDispatch");
        check(mn.contains("initializeMessage"), "method initializeMessage");
        check(mn.contains("isDIIRequest"), "method isDIIRequest");
        check(mn.contains("isDifferentAddrDispositionRequestedReply"), "method isDifferentAddrDispositionRequestedReply");
        check(mn.contains("isLocationForwardReply"), "method isLocationForwardReply");
        check(mn.contains("isOneWay"), "method isOneWay");
        check(mn.contains("isSystemExceptionReply"), "method isSystemExceptionReply");
        check(mn.contains("isUserExceptionReply"), "method isUserExceptionReply");
        check(mn.contains("sendCancelRequestIfFinalFragmentNotSent"), "method sendCancelRequestIfFinalFragmentNotSent");
        check(mn.contains("sentFragment"), "method sentFragment");
        check(mn.contains("sentFullMessage"), "method sentFullMessage");
        check(mn.contains("setDIIException"), "method setDIIException");
        check(mn.contains("setDIIInfo"), "method setDIIInfo");
        check(mn.contains("setDispatchBuffer"), "method setDispatchBuffer");
        check(mn.contains("setDispatchHeader"), "method setDispatchHeader");
        check(mn.contains("setExecutePIInResponseConstructor"), "method setExecutePIInResponseConstructor");
        check(mn.contains("setExecuteRemoveThreadInfoInResponseConstructor"), "method setExecuteRemoveThreadInfoInResponseConstructor");
        check(mn.contains("setExecuteReturnServantInResponseConstructor"), "method setExecuteReturnServantInResponseConstructor");
        check(mn.contains("setInputObject"), "method setInputObject");
        check(mn.contains("setOutputObject"), "method setOutputObject");
        check(mn.contains("setProtocolHandler"), "method setProtocolHandler");
        check(mn.contains("setReplyExceptionDetailMessage"), "method setReplyExceptionDetailMessage");
        check(mn.contains("setReplyHeader"), "method setReplyHeader");
        check(mn.contains("unmarshalDIIUserException"), "method unmarshalDIIUserException");
        check(mn.contains("waitForResponse"), "method waitForResponse");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
