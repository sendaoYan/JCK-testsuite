/*
 * @test
 * @summary com.sun.corba.se.spi.orb.ORBData conformance: type shape via reflection (headless-safe).
 * @run main ORBDataTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ORBDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.orb.ORBData", false, ORBDataTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 48, "public declared method count");
        check(mn.contains("acceptorSocketType"), "method acceptorSocketType");
        check(mn.contains("acceptorSocketUseSelectThreadToWait"), "method acceptorSocketUseSelectThreadToWait");
        check(mn.contains("acceptorSocketUseWorkerThreadForEvent"), "method acceptorSocketUseWorkerThreadForEvent");
        check(mn.contains("alwaysSendCodeSetServiceContext"), "method alwaysSendCodeSetServiceContext");
        check(mn.contains("connectionSocketType"), "method connectionSocketType");
        check(mn.contains("connectionSocketUseSelectThreadToWait"), "method connectionSocketUseSelectThreadToWait");
        check(mn.contains("connectionSocketUseWorkerThreadForEvent"), "method connectionSocketUseWorkerThreadForEvent");
        check(mn.contains("disableDirectByteBufferUse"), "method disableDirectByteBufferUse");
        check(mn.contains("getAcceptors"), "method getAcceptors");
        check(mn.contains("getBadServerIdHandler"), "method getBadServerIdHandler");
        check(mn.contains("getCodeSetComponentInfo"), "method getCodeSetComponentInfo");
        check(mn.contains("getCorbaContactInfoListFactory"), "method getCorbaContactInfoListFactory");
        check(mn.contains("getGIOPAddressDisposition"), "method getGIOPAddressDisposition");
        check(mn.contains("getGIOPBuffMgrStrategy"), "method getGIOPBuffMgrStrategy");
        check(mn.contains("getGIOPBufferSize"), "method getGIOPBufferSize");
        check(mn.contains("getGIOPFragmentSize"), "method getGIOPFragmentSize");
        check(mn.contains("getGIOPTargetAddressPreference"), "method getGIOPTargetAddressPreference");
        check(mn.contains("getGIOPVersion"), "method getGIOPVersion");
        check(mn.contains("getHighWaterMark"), "method getHighWaterMark");
        check(mn.contains("getIIOPPrimaryToContactInfo"), "method getIIOPPrimaryToContactInfo");
        check(mn.contains("getIORToSocketInfo"), "method getIORToSocketInfo");
        check(mn.contains("getLegacySocketFactory"), "method getLegacySocketFactory");
        check(mn.contains("getListenOnAllInterfaces"), "method getListenOnAllInterfaces");
        check(mn.contains("getLowWaterMark"), "method getLowWaterMark");
        check(mn.contains("getNumberToReclaim"), "method getNumberToReclaim");
        check(mn.contains("getORBDebugFlags"), "method getORBDebugFlags");
        check(mn.contains("getORBDefaultInitialReference"), "method getORBDefaultInitialReference");
        check(mn.contains("getORBId"), "method getORBId");
        check(mn.contains("getORBInitialHost"), "method getORBInitialHost");
        check(mn.contains("getORBInitialPort"), "method getORBInitialPort");
        check(mn.contains("getORBInitialReferences"), "method getORBInitialReferences");
        check(mn.contains("getORBInitializers"), "method getORBInitializers");
        check(mn.contains("getORBServerHost"), "method getORBServerHost");
        check(mn.contains("getORBServerIdPropertySpecified"), "method getORBServerIdPropertySpecified");
        check(mn.contains("getORBServerPort"), "method getORBServerPort");
        check(mn.contains("getPersistentPortInitialized"), "method getPersistentPortInitialized");
        check(mn.contains("getPersistentServerId"), "method getPersistentServerId");
        check(mn.contains("getPersistentServerIdInitialized"), "method getPersistentServerIdInitialized");
        check(mn.contains("getPersistentServerPort"), "method getPersistentServerPort");
        check(mn.contains("getServerIsORBActivated"), "method getServerIsORBActivated");
        check(mn.contains("getSocketFactory"), "method getSocketFactory");
        check(mn.contains("getTransportTCPReadTimeouts"), "method getTransportTCPReadTimeouts");
        check(mn.contains("getUserSpecifiedListenPorts"), "method getUserSpecifiedListenPorts");
        check(mn.contains("isJavaSerializationEnabled"), "method isJavaSerializationEnabled");
        check(mn.contains("isLocalOptimizationAllowed"), "method isLocalOptimizationAllowed");
        check(mn.contains("useByteOrderMarkers"), "method useByteOrderMarkers");
        check(mn.contains("useByteOrderMarkersInEncapsulations"), "method useByteOrderMarkersInEncapsulations");
        check(mn.contains("useRepId"), "method useRepId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
