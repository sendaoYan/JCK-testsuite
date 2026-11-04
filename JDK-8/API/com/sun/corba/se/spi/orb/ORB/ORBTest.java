/*
 * @test
 * @summary com.sun.corba.se.spi.orb.ORB conformance: type shape via reflection (headless-safe).
 * @run main ORBTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ORBTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.orb.ORB", false, ORBTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.org.omg.CORBA.ORB"), "superclass com.sun.corba.se.org.omg.CORBA.ORB");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.corba.TypeCodeFactory"), "interface com.sun.corba.se.impl.corba.TypeCodeFactory");
        check(in.contains("com.sun.corba.se.pept.broker.Broker"), "interface com.sun.corba.se.pept.broker.Broker");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 56, "public declared method count");
        check(mn.contains("checkShutdownState"), "method checkShutdownState");
        check(mn.contains("destroy"), "method destroy");
        check(mn.contains("finishedDispatch"), "method finishedDispatch");
        check(mn.contains("getByteBufferPool"), "method getByteBufferPool");
        check(mn.contains("getClientDelegateFactory"), "method getClientDelegateFactory");
        check(mn.contains("getCopierManager"), "method getCopierManager");
        check(mn.contains("getCorbaContactInfoListFactory"), "method getCorbaContactInfoListFactory");
        check(mn.contains("getCorbaTransportManager"), "method getCorbaTransportManager");
        check(mn.contains("getFVDCodeBaseIOR"), "method getFVDCodeBaseIOR");
        check(mn.contains("getLegacyServerSocketManager"), "method getLegacyServerSocketManager");
        check(mn.contains("getLocalResolver"), "method getLocalResolver");
        check(mn.contains("getLogWrapper"), "method getLogWrapper");
        check(mn.contains("getLogger"), "method getLogger");
        check(mn.contains("getMonitoringManager"), "method getMonitoringManager");
        check(mn.contains("getORBData"), "method getORBData");
        check(mn.contains("getORBVersion"), "method getORBVersion");
        check(mn.contains("getObjectKeyFactory"), "method getObjectKeyFactory");
        check(mn.contains("getPIHandler"), "method getPIHandler");
        check(mn.contains("getPresentationManager"), "method getPresentationManager");
        check(mn.contains("getRequestDispatcherRegistry"), "method getRequestDispatcherRegistry");
        check(mn.contains("getResolver"), "method getResolver");
        check(mn.contains("getServiceContextRegistry"), "method getServiceContextRegistry");
        check(mn.contains("getStubFactoryFactory"), "method getStubFactoryFactory");
        check(mn.contains("getTaggedComponentFactoryFinder"), "method getTaggedComponentFactoryFinder");
        check(mn.contains("getTaggedProfileFactoryFinder"), "method getTaggedProfileFactoryFinder");
        check(mn.contains("getTaggedProfileTemplateFactoryFinder"), "method getTaggedProfileTemplateFactoryFinder");
        check(mn.contains("getThreadPoolManager"), "method getThreadPoolManager");
        check(mn.contains("getTransientServerId"), "method getTransientServerId");
        check(mn.contains("getTypeCode"), "method getTypeCode");
        check(mn.contains("getURLOperation"), "method getURLOperation");
        check(mn.contains("get_primitive_tc"), "method get_primitive_tc");
        check(mn.contains("handleBadServerId"), "method handleBadServerId");
        check(mn.contains("initBadServerIdHandler"), "method initBadServerIdHandler");
        check(mn.contains("isDuringDispatch"), "method isDuringDispatch");
        check(mn.contains("isLocalHost"), "method isLocalHost");
        check(mn.contains("isLocalServerId"), "method isLocalServerId");
        check(mn.contains("notifyORB"), "method notifyORB");
        check(mn.contains("peekInvocationInfo"), "method peekInvocationInfo");
        check(mn.contains("popInvocationInfo"), "method popInvocationInfo");
        check(mn.contains("pushInvocationInfo"), "method pushInvocationInfo");
        check(mn.contains("setBadServerIdHandler"), "method setBadServerIdHandler");
        check(mn.contains("setClientDelegateFactory"), "method setClientDelegateFactory");
        check(mn.contains("setCorbaContactInfoListFactory"), "method setCorbaContactInfoListFactory");
        check(mn.contains("setINSDelegate"), "method setINSDelegate");
        check(mn.contains("setLocalResolver"), "method setLocalResolver");
        check(mn.contains("setORBVersion"), "method setORBVersion");
        check(mn.contains("setObjectKeyFactory"), "method setObjectKeyFactory");
        check(mn.contains("setResolver"), "method setResolver");
        check(mn.contains("setThreadPoolManager"), "method setThreadPoolManager");
        check(mn.contains("setTypeCode"), "method setTypeCode");
        check(mn.contains("setURLOperation"), "method setURLOperation");
        check(mn.contains("set_parameters"), "method set_parameters");
        check(mn.contains("startingDispatch"), "method startingDispatch");
        check(mn.contains("staticGetLogWrapper"), "method staticGetLogWrapper");
        check(mn.contains("staticGetLogger"), "method staticGetLogger");
        check(mn.contains("validateIORClass"), "method validateIORClass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
