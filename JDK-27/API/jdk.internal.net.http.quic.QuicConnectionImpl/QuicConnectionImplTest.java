/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicConnectionImpl conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicConnectionImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicConnectionImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicConnectionImpl", false, QuicConnectionImplTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicConnectionImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.quic.QuicConnection"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.quic.QuicPacketReceiver"), "iface:jdk.internal.net.http.quic.QuicPacketReceiver");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("accepts"), "method:accepts");
        check(methods.contains("activeResetTokens"), "method:activeResetTokens");
        check(methods.contains("addRemoteStreamListener"), "method:addRemoteStreamListener");
        check(methods.contains("allocateDatagramForEncryption"), "method:allocateDatagramForEncryption");
        check(methods.contains("buildInitialParameters"), "method:buildInitialParameters");
        check(methods.contains("channel"), "method:channel");
        check(methods.contains("closeIncoming"), "method:closeIncoming");
        check(methods.contains("codingContext"), "method:codingContext");
        check(methods.contains("completeHandshakeCF"), "method:completeHandshakeCF");
        check(methods.contains("connectionIdLength"), "method:connectionIdLength");
        check(methods.contains("connectionIds"), "method:connectionIds");
        check(methods.contains("connectionTerminator"), "method:connectionTerminator");
        check(methods.contains("consumeQuicParameters"), "method:consumeQuicParameters");
        check(methods.contains("continueHandshake"), "method:continueHandshake");
        check(methods.contains("continueHandshake0"), "method:continueHandshake0");
        check(methods.contains("datagramDiscarded"), "method:datagramDiscarded");
        check(methods.contains("datagramDropped"), "method:datagramDropped");
        check(methods.contains("datagramReleased"), "method:datagramReleased");
        check(methods.contains("datagramSent"), "method:datagramSent");
        check(methods.contains("dbgTag"), "method:dbgTag");
        check(methods.contains("decoder"), "method:decoder");
        check(methods.contains("emitter"), "method:emitter");
        check(methods.contains("encoder"), "method:encoder");
        check(methods.contains("endpoint"), "method:endpoint");
        check(methods.contains("enqueue1RTTFrame"), "method:enqueue1RTTFrame");
        check(methods.contains("findStream"), "method:findStream");
        check(methods.contains("futureTerminationCause"), "method:futureTerminationCause");
        check(methods.contains("getIncomingInitialPacketSourceId"), "method:getIncomingInitialPacketSourceId");
        check(methods.contains("getLocalActiveConnIDLimit"), "method:getLocalActiveConnIDLimit");
        check(methods.contains("getMaxDatagramSize"), "method:getMaxDatagramSize");
        check(methods.contains("getOutgoingByteBuffer"), "method:getOutgoingByteBuffer");
        check(methods.contains("getPeerActiveConnIDLimit"), "method:getPeerActiveConnIDLimit");
        check(methods.contains("getTLSEngine"), "method:getTLSEngine");
        check(methods.contains("handleIncomingPeerTransportParams"), "method:handleIncomingPeerTransportParams");
        check(methods.contains("handshakeFlow"), "method:handshakeFlow");
        check(methods.contains("handshakeReachedPeer"), "method:handshakeReachedPeer");
        check(methods.contains("incoming1RTTFrame"), "method:incoming1RTTFrame");
        check(methods.contains("incomingHandshakeFrame"), "method:incomingHandshakeFrame");
        check(methods.contains("incomingInitialFrame"), "method:incomingInitialFrame");
        check(methods.contains("increaseProcessedData"), "method:increaseProcessedData");
        check(methods.contains("increaseReceivedData"), "method:increaseReceivedData");
        check(methods.contains("internalProcessIncoming"), "method:internalProcessIncoming");
        check(methods.contains("isClientConnection"), "method:isClientConnection");
        check(methods.contains("isDatagramComplete"), "method:isDatagramComplete");
        check(methods.contains("isExistingStreamId"), "method:isExistingStreamId");
        check(methods.contains("isLocalStream"), "method:isLocalStream");
        check(methods.contains("isOpen"), "method:isOpen");
        check(methods.contains("largestAckedPN"), "method:largestAckedPN");
        check(methods.contains("largestProcessedPN"), "method:largestProcessedPN");
        check(methods.contains("localAddress"), "method:localAddress");
        check(methods.contains("localConnectionId"), "method:localConnectionId");
        check(methods.contains("localTransportParameters"), "method:localTransportParameters");
        check(methods.contains("logTag"), "method:logTag");
        check(methods.contains("loggableState"), "method:loggableState");
        check(methods.contains("markVersionNegotiated"), "method:markVersionNegotiated");
        check(methods.contains("newLocalTransportParameters"), "method:newLocalTransportParameters");
        check(methods.contains("nextMaxStreamsLimit"), "method:nextMaxStreamsLimit");
        check(methods.contains("notifyTerminalState"), "method:notifyTerminalState");
        check(methods.contains("onHandshakeCompletion"), "method:onHandshakeCompletion");
        check(methods.contains("onProcessingError"), "method:onProcessingError");
        check(methods.contains("onWriteError"), "method:onWriteError");
        check(methods.contains("openNewLocalBidiStream"), "method:openNewLocalBidiStream");
        check(methods.contains("openNewLocalUniStream"), "method:openNewLocalUniStream");
        check(methods.contains("openOrGetRemoteStream"), "method:openOrGetRemoteStream");
        check(methods.contains("originalServerConnId"), "method:originalServerConnId");
        check(methods.contains("packetNumberSpaces"), "method:packetNumberSpaces");
        check(methods.contains("packetSpace"), "method:packetSpace");
        check(methods.contains("peerAddress"), "method:peerAddress");
        check(methods.contains("peerConnectionId"), "method:peerConnectionId");
        check(methods.contains("peerPtoMs"), "method:peerPtoMs");
        check(methods.contains("peerTransportParameters"), "method:peerTransportParameters");
        check(methods.contains("processDecrypted"), "method:processDecrypted");
        check(methods.contains("processHandshakePacket"), "method:processHandshakePacket");
        check(methods.contains("processHandshakePacketPayload"), "method:processHandshakePacketPayload");
        check(methods.contains("processIncoming"), "method:processIncoming");
        check(methods.contains("processInitialPacket"), "method:processInitialPacket");
        check(methods.contains("processInitialPacketPayload"), "method:processInitialPacketPayload");
        check(methods.contains("processOneRTTPacket"), "method:processOneRTTPacket");
        check(methods.contains("processRetryPacket"), "method:processRetryPacket");
        check(methods.contains("processStatelessReset"), "method:processStatelessReset");
        check(methods.contains("processVersionNegotiationPacket"), "method:processVersionNegotiationPacket");
        check(methods.contains("pushDatagram"), "method:pushDatagram");
        check(methods.contains("quicInstance"), "method:quicInstance");
        check(methods.contains("quicStreams"), "method:quicStreams");
        check(methods.contains("quicVersion"), "method:quicVersion");
        check(methods.contains("removeRemoteStreamListener"), "method:removeRemoteStreamListener");
        check(methods.contains("requestResetStream"), "method:requestResetStream");
        check(methods.contains("requestSendMaxStreamData"), "method:requestSendMaxStreamData");
        check(methods.contains("requestSendPing"), "method:requestSendPing");
        check(methods.contains("runAppPacketSpaceTransmitter"), "method:runAppPacketSpaceTransmitter");
        check(methods.contains("scheduleStopSendingFrame"), "method:scheduleStopSendingFrame");
        check(methods.contains("send1RTTPacket"), "method:send1RTTPacket");
        check(methods.contains("setIntParamIfNotSet"), "method:setIntParamIfNotSet");
        check(methods.contains("shutdown"), "method:shutdown");
        check(methods.contains("startHandshake"), "method:startHandshake");
        check(methods.contains("startInitialTimer"), "method:startInitialTimer");
        check(methods.contains("stateHandle"), "method:stateHandle");
        check(methods.contains("streamDataAvailableForSending"), "method:streamDataAvailableForSending");
        check(methods.contains("streamDbgTag"), "method:streamDbgTag");
        check(methods.contains("switchVersion"), "method:switchVersion");
        check(methods.contains("terminationCause"), "method:terminationCause");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("uniqueId"), "method:uniqueId");
        check(methods.contains("updatePeerConnectionId"), "method:updatePeerConnectionId");
        check(methods.contains("verifyToken"), "method:verifyToken");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("DEFAULT_DATAGRAM_SIZE"), "field:DEFAULT_DATAGRAM_SIZE");
        check(fields.contains("DEFAULT_INITIAL_MAX_DATA"), "field:DEFAULT_INITIAL_MAX_DATA");
        check(fields.contains("DEFAULT_INITIAL_STREAM_MAX_DATA"), "field:DEFAULT_INITIAL_STREAM_MAX_DATA");
        check(fields.contains("DEFAULT_MAX_BIDI_STREAMS"), "field:DEFAULT_MAX_BIDI_STREAMS");
        check(fields.contains("DEFAULT_MAX_INITIAL_TIMEOUT"), "field:DEFAULT_MAX_INITIAL_TIMEOUT");
        check(fields.contains("DEFAULT_MAX_UNI_STREAMS"), "field:DEFAULT_MAX_UNI_STREAMS");
        check(fields.contains("MAX_STREAMS_VALUE_LIMIT"), "field:MAX_STREAMS_VALUE_LIMIT");
        check(fields.contains("RESET_TOKEN_LENGTH"), "field:RESET_TOKEN_LENGTH");
        check(fields.contains("SMALLEST_MAXIMUM_DATAGRAM_SIZE"), "field:SMALLEST_MAXIMUM_DATAGRAM_SIZE");
        check(fields.contains("USE_DIRECT_BUFFER_POOL"), "field:USE_DIRECT_BUFFER_POOL");
        check(fields.contains("debug"), "field:debug");
        check(fields.contains("endpoint"), "field:endpoint");
        check(fields.contains("idleTimeoutManager"), "field:idleTimeoutManager");
        check(fields.contains("outgoing1RTTFrames"), "field:outgoing1RTTFrames");
        check(fields.contains("streams"), "field:streams");
        check(fields.contains("transportParams"), "field:transportParams");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
