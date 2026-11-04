/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicConnectionStreams conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main QuicConnectionStreamsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicConnectionStreamsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicConnectionStreams", false, QuicConnectionStreamsTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicConnectionStreams"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addRemoteStreamListener"), "method:addRemoteStreamListener");
        check(methods.contains("createNewLocalBidiStream"), "method:createNewLocalBidiStream");
        check(methods.contains("createNewLocalUniStream"), "method:createNewLocalUniStream");
        check(methods.contains("enqueueForSending"), "method:enqueueForSending");
        check(methods.contains("enqueueStreamDataBlocked"), "method:enqueueStreamDataBlocked");
        check(methods.contains("findStream"), "method:findStream");
        check(methods.contains("getOrCreateRemoteStream"), "method:getOrCreateRemoteStream");
        check(methods.contains("hasAvailableData"), "method:hasAvailableData");
        check(methods.contains("hasBlockedStreams"), "method:hasBlockedStreams");
        check(methods.contains("hasControlFrames"), "method:hasControlFrames");
        check(methods.contains("isLocalBidi"), "method:isLocalBidi");
        check(methods.contains("isLocalUni"), "method:isLocalUni");
        check(methods.contains("isReceivingStream"), "method:isReceivingStream");
        check(methods.contains("isRemoteBidi"), "method:isRemoteBidi");
        check(methods.contains("isRemoteUni"), "method:isRemoteUni");
        check(methods.contains("isSendingStream"), "method:isSendingStream");
        check(methods.contains("newLocalTransportParameters"), "method:newLocalTransportParameters");
        check(methods.contains("newPeerTransportParameters"), "method:newPeerTransportParameters");
        check(methods.contains("nextMaxStreamsLimit"), "method:nextMaxStreamsLimit");
        check(methods.contains("notifyTerminalState"), "method:notifyTerminalState");
        check(methods.contains("peekNextStreamId"), "method:peekNextStreamId");
        check(methods.contains("peerStreamsBlocked"), "method:peerStreamsBlocked");
        check(methods.contains("processIncomingFrame"), "method:processIncomingFrame");
        check(methods.contains("produceFramesToSend"), "method:produceFramesToSend");
        check(methods.contains("quicStreams"), "method:quicStreams");
        check(methods.contains("removeRemoteStreamListener"), "method:removeRemoteStreamListener");
        check(methods.contains("requestResetStream"), "method:requestResetStream");
        check(methods.contains("requestSendMaxStreamData"), "method:requestSendMaxStreamData");
        check(methods.contains("scheduleStopSendingFrame"), "method:scheduleStopSendingFrame");
        check(methods.contains("setMaxStreamData"), "method:setMaxStreamData");
        check(methods.contains("stopSendingReceived"), "method:stopSendingReceived");
        check(methods.contains("streamDataSentAcknowledged"), "method:streamDataSentAcknowledged");
        check(methods.contains("streamResetAcknowledged"), "method:streamResetAcknowledged");
        check(methods.contains("terminate"), "method:terminate");
        check(methods.contains("tryIncreaseStreamLimit"), "method:tryIncreaseStreamLimit");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
