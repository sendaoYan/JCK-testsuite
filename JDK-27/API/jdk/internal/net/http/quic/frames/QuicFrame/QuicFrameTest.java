/*
 * @test
 * @summary jdk.internal.net.http.quic.frames.QuicFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.frames:+open
 * @run main QuicFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.frames.QuicFrame", false, QuicFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.frames.QuicFrame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("decode"), "method:decode");
        check(methods.contains("decodeVLField"), "method:decodeVLField");
        check(methods.contains("decodeVLFieldAsInt"), "method:decodeVLFieldAsInt");
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("encodeVLField"), "method:encodeVLField");
        check(methods.contains("frameClassOf"), "method:frameClassOf");
        check(methods.contains("frameType"), "method:frameType");
        check(methods.contains("frameTypeOf"), "method:frameTypeOf");
        check(methods.contains("getTypeField"), "method:getTypeField");
        check(methods.contains("getVLFieldLengthFor"), "method:getVLFieldLengthFor");
        check(methods.contains("isAckEliciting"), "method:isAckEliciting");
        check(methods.contains("isValidIn"), "method:isValidIn");
        check(methods.contains("putByteBuffer"), "method:putByteBuffer");
        check(methods.contains("requireVLRange"), "method:requireVLRange");
        check(methods.contains("size"), "method:size");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("validateRemainingLength"), "method:validateRemainingLength");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ACK"), "field:ACK");
        check(fields.contains("CONNECTION_CLOSE"), "field:CONNECTION_CLOSE");
        check(fields.contains("CRYPTO"), "field:CRYPTO");
        check(fields.contains("DATA_BLOCKED"), "field:DATA_BLOCKED");
        check(fields.contains("HANDSHAKE_DONE"), "field:HANDSHAKE_DONE");
        check(fields.contains("MAX_DATA"), "field:MAX_DATA");
        check(fields.contains("MAX_STREAMS"), "field:MAX_STREAMS");
        check(fields.contains("MAX_STREAM_DATA"), "field:MAX_STREAM_DATA");
        check(fields.contains("MAX_VL_INTEGER"), "field:MAX_VL_INTEGER");
        check(fields.contains("NEW_CONNECTION_ID"), "field:NEW_CONNECTION_ID");
        check(fields.contains("NEW_TOKEN"), "field:NEW_TOKEN");
        check(fields.contains("PADDING"), "field:PADDING");
        check(fields.contains("PATH_CHALLENGE"), "field:PATH_CHALLENGE");
        check(fields.contains("PATH_RESPONSE"), "field:PATH_RESPONSE");
        check(fields.contains("PING"), "field:PING");
        check(fields.contains("RESET_STREAM"), "field:RESET_STREAM");
        check(fields.contains("RETIRE_CONNECTION_ID"), "field:RETIRE_CONNECTION_ID");
        check(fields.contains("STOP_SENDING"), "field:STOP_SENDING");
        check(fields.contains("STREAM"), "field:STREAM");
        check(fields.contains("STREAMS_BLOCKED"), "field:STREAMS_BLOCKED");
        check(fields.contains("STREAM_DATA_BLOCKED"), "field:STREAM_DATA_BLOCKED");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
