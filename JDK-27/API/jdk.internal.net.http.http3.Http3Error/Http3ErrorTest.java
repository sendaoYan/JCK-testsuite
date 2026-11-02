/*
 * @test
 * @summary jdk.internal.net.http.http3.Http3Error conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.http3:+open
 * @run main Http3ErrorTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Http3ErrorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.http3.Http3Error", false, Http3ErrorTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.http3.Http3Error"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("code"), "method:code");
        check(methods.contains("fromCode"), "method:fromCode");
        check(methods.contains("isNoError"), "method:isNoError");
        check(methods.contains("stringForCode"), "method:stringForCode");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("H3_CLOSED_CRITICAL_STREAM"), "field:H3_CLOSED_CRITICAL_STREAM");
        check(fields.contains("H3_CONNECT_ERROR"), "field:H3_CONNECT_ERROR");
        check(fields.contains("H3_EXCESSIVE_LOAD"), "field:H3_EXCESSIVE_LOAD");
        check(fields.contains("H3_FRAME_ERROR"), "field:H3_FRAME_ERROR");
        check(fields.contains("H3_FRAME_UNEXPECTED"), "field:H3_FRAME_UNEXPECTED");
        check(fields.contains("H3_GENERAL_PROTOCOL_ERROR"), "field:H3_GENERAL_PROTOCOL_ERROR");
        check(fields.contains("H3_ID_ERROR"), "field:H3_ID_ERROR");
        check(fields.contains("H3_INTERNAL_ERROR"), "field:H3_INTERNAL_ERROR");
        check(fields.contains("H3_MESSAGE_ERROR"), "field:H3_MESSAGE_ERROR");
        check(fields.contains("H3_MISSING_SETTINGS"), "field:H3_MISSING_SETTINGS");
        check(fields.contains("H3_NO_ERROR"), "field:H3_NO_ERROR");
        check(fields.contains("H3_REQUEST_CANCELLED"), "field:H3_REQUEST_CANCELLED");
        check(fields.contains("H3_REQUEST_INCOMPLETE"), "field:H3_REQUEST_INCOMPLETE");
        check(fields.contains("H3_REQUEST_REJECTED"), "field:H3_REQUEST_REJECTED");
        check(fields.contains("H3_SETTINGS_ERROR"), "field:H3_SETTINGS_ERROR");
        check(fields.contains("H3_STREAM_CREATION_ERROR"), "field:H3_STREAM_CREATION_ERROR");
        check(fields.contains("H3_VERSION_FALLBACK"), "field:H3_VERSION_FALLBACK");
        check(fields.contains("QPACK_DECODER_STREAM_ERROR"), "field:QPACK_DECODER_STREAM_ERROR");
        check(fields.contains("QPACK_DECOMPRESSION_FAILED"), "field:QPACK_DECOMPRESSION_FAILED");
        check(fields.contains("QPACK_ENCODER_STREAM_ERROR"), "field:QPACK_ENCODER_STREAM_ERROR");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("H3_CLOSED_CRITICAL_STREAM"), "enum-const:H3_CLOSED_CRITICAL_STREAM");
        check(enumConsts.contains("H3_CONNECT_ERROR"), "enum-const:H3_CONNECT_ERROR");
        check(enumConsts.contains("H3_EXCESSIVE_LOAD"), "enum-const:H3_EXCESSIVE_LOAD");
        check(enumConsts.contains("H3_FRAME_ERROR"), "enum-const:H3_FRAME_ERROR");
        check(enumConsts.contains("H3_FRAME_UNEXPECTED"), "enum-const:H3_FRAME_UNEXPECTED");
        check(enumConsts.contains("H3_GENERAL_PROTOCOL_ERROR"), "enum-const:H3_GENERAL_PROTOCOL_ERROR");
        check(enumConsts.contains("H3_ID_ERROR"), "enum-const:H3_ID_ERROR");
        check(enumConsts.contains("H3_INTERNAL_ERROR"), "enum-const:H3_INTERNAL_ERROR");
        check(enumConsts.contains("H3_MESSAGE_ERROR"), "enum-const:H3_MESSAGE_ERROR");
        check(enumConsts.contains("H3_MISSING_SETTINGS"), "enum-const:H3_MISSING_SETTINGS");
        check(enumConsts.contains("H3_NO_ERROR"), "enum-const:H3_NO_ERROR");
        check(enumConsts.contains("H3_REQUEST_CANCELLED"), "enum-const:H3_REQUEST_CANCELLED");
        check(enumConsts.contains("H3_REQUEST_INCOMPLETE"), "enum-const:H3_REQUEST_INCOMPLETE");
        check(enumConsts.contains("H3_REQUEST_REJECTED"), "enum-const:H3_REQUEST_REJECTED");
        check(enumConsts.contains("H3_SETTINGS_ERROR"), "enum-const:H3_SETTINGS_ERROR");
        check(enumConsts.contains("H3_STREAM_CREATION_ERROR"), "enum-const:H3_STREAM_CREATION_ERROR");
        check(enumConsts.contains("H3_VERSION_FALLBACK"), "enum-const:H3_VERSION_FALLBACK");
        check(enumConsts.contains("QPACK_DECODER_STREAM_ERROR"), "enum-const:QPACK_DECODER_STREAM_ERROR");
        check(enumConsts.contains("QPACK_DECOMPRESSION_FAILED"), "enum-const:QPACK_DECOMPRESSION_FAILED");
        check(enumConsts.contains("QPACK_ENCODER_STREAM_ERROR"), "enum-const:QPACK_ENCODER_STREAM_ERROR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
