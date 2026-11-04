/*
 * @test
 * @summary com.sun.xml.internal.ws.api.message.Packet conformance: reflection (kind, superclass, interfaces, methods).
 * @run main PacketTest
 */

import java.lang.reflect.Modifier;


public class PacketTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.api.message.Packet", false, PacketTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.api.message.Packet"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass().getName().equals("com.oracle.webservices.internal.api.message.BaseDistributedPropertySet"), "superclass com.oracle.webservices.internal.api.message.BaseDistributedPropertySet");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "com.oracle.webservices.internal.api.message.MessageContext"), "implements com.oracle.webservices.internal.api.message.MessageContext");
        check(has(ifs, "com.sun.xml.internal.ws.api.message.MessageMetadata"), "implements com.sun.xml.internal.ws.api.message.MessageMetadata");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("addSatellite"), "method addSatellite");
        check(mn.contains("asMapIncludingInvocationProperties"), "method asMapIncludingInvocationProperties");
        check(mn.contains("checkMtomAcceptable"), "method checkMtomAcceptable");
        check(mn.contains("copy"), "method copy");
        check(mn.contains("copyPropertiesTo"), "method copyPropertiesTo");
        check(mn.contains("copySatelliteInto"), "method copySatelliteInto");
        check(mn.contains("createClientResponse"), "method createClientResponse");
        check(mn.contains("createResponse"), "method createResponse");
        check(mn.contains("createServerResponse"), "method createServerResponse");
        check(mn.contains("getApplicationScopePropertyNames"), "method getApplicationScopePropertyNames");
        check(mn.contains("getAsSOAPMessage"), "method getAsSOAPMessage");
        check(mn.contains("getBinding"), "method getBinding");
        check(mn.contains("getCodec"), "method getCodec");
        check(mn.contains("getContentNegotiationString"), "method getContentNegotiationString");
        check(mn.contains("getContentType"), "method getContentType");
        check(mn.contains("getEndPointAddressString"), "method getEndPointAddressString");
        check(mn.contains("getFastInfosetAcceptable"), "method getFastInfosetAcceptable");
        check(mn.contains("getHandlerScopePropertyNames"), "method getHandlerScopePropertyNames");
        check(mn.contains("getInternalContentType"), "method getInternalContentType");
        check(mn.contains("getInternalMessage"), "method getInternalMessage");
        check(mn.contains("getMessage"), "method getMessage");
        check(mn.contains("getMtomAcceptable"), "method getMtomAcceptable");
        check(mn.contains("getMtomFeature"), "method getMtomFeature");
        check(mn.contains("getMtomRequest"), "method getMtomRequest");
        check(mn.contains("getPropertyMap"), "method getPropertyMap");
        check(mn.contains("getReferenceParameters"), "method getReferenceParameters");
        check(mn.contains("getSOAPMessage"), "method getSOAPMessage");
        check(mn.contains("getState"), "method getState");
        check(mn.contains("getWSDLOperation"), "method getWSDLOperation");
        check(mn.contains("getWSDLOperationMapping"), "method getWSDLOperationMapping");
        check(mn.contains("isRequestReplyMEP"), "method isRequestReplyMEP");
        check(mn.contains("keepTransportBackChannelOpen"), "method keepTransportBackChannelOpen");
        check(mn.contains("relateClientResponse"), "method relateClientResponse");
        check(mn.contains("relateServerResponse"), "method relateServerResponse");
        check(mn.contains("removeSatellite"), "method removeSatellite");
        check(mn.contains("setContentNegotiationString"), "method setContentNegotiationString");
        check(mn.contains("setContentType"), "method setContentType");
        check(mn.contains("setEndPointAddressString"), "method setEndPointAddressString");
        check(mn.contains("setFastInfosetDisabled"), "method setFastInfosetDisabled");
        check(mn.contains("setMessage"), "method setMessage");
        check(mn.contains("setMtomFeature"), "method setMtomFeature");
        check(mn.contains("setMtomRequest"), "method setMtomRequest");
        check(mn.contains("setRequestReplyMEP"), "method setRequestReplyMEP");
        check(mn.contains("setResponseMessage"), "method setResponseMessage");
        check(mn.contains("setState"), "method setState");
        check(mn.contains("setWSDLOperation"), "method setWSDLOperation");
        check(mn.contains("shouldUseMtom"), "method shouldUseMtom");
        check(mn.contains("toShortString"), "method toShortString");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("writeTo"), "method writeTo");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("HA_INFO"), "field HA_INFO");
        check(fn.contains("INBOUND_TRANSPORT_HEADERS"), "field INBOUND_TRANSPORT_HEADERS");
        check(fn.contains("OUTBOUND_TRANSPORT_HEADERS"), "field OUTBOUND_TRANSPORT_HEADERS");
        check(fn.contains("acceptableMimeTypes"), "field acceptableMimeTypes");
        check(fn.contains("codec"), "field codec");
        check(fn.contains("component"), "field component");
        check(fn.contains("contentNegotiation"), "field contentNegotiation");
        check(fn.contains("endpoint"), "field endpoint");
        check(fn.contains("endpointAddress"), "field endpointAddress");
        check(fn.contains("expectReply"), "field expectReply");
        check(fn.contains("handlerConfig"), "field handlerConfig");
        check(fn.contains("invocationProperties"), "field invocationProperties");
        check(fn.contains("isAdapterDeliversNonAnonymousResponse"), "field isAdapterDeliversNonAnonymousResponse");
        check(fn.contains("isOneWay"), "field isOneWay");
        check(fn.contains("isSynchronousMEP"), "field isSynchronousMEP");
        check(fn.contains("nonNullAsyncHandlerGiven"), "field nonNullAsyncHandlerGiven");
        check(fn.contains("packetTakesPriorityOverRequestContext"), "field packetTakesPriorityOverRequestContext");
        check(fn.contains("proxy"), "field proxy");
        check(fn.contains("soapAction"), "field soapAction");
        check(fn.contains("transportBackChannel"), "field transportBackChannel");
        check(fn.contains("wasTransportSecure"), "field wasTransportSecure");
        check(fn.contains("webServiceContextDelegate"), "field webServiceContextDelegate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
