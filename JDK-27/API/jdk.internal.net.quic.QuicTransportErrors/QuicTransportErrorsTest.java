/*
 * @test
 * @summary jdk.internal.net.quic.QuicTransportErrors conformance: internal package reflection.
 * @modules java.base/jdk.internal.net.quic:+open
 * @run main QuicTransportErrorsTest
 */
public class QuicTransportErrorsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.quic.QuicTransportErrors", false, QuicTransportErrorsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "code"), "method code");
        check(hasMethod(c, "from"), "method from");
        check(hasMethod(c, "hasCode"), "method hasCode");
        check(hasMethod(c, "hasRange"), "method hasRange");
        check(hasMethod(c, "ofCode"), "method ofCode");
        check(hasMethod(c, "to"), "method to");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "AEAD_LIMIT_REACHED"), "field AEAD_LIMIT_REACHED");
        check(hasField(c, "APPLICATION_ERROR"), "field APPLICATION_ERROR");
        check(hasField(c, "CONNECTION_ID_LIMIT_ERROR"), "field CONNECTION_ID_LIMIT_ERROR");
        check(hasField(c, "CONNECTION_REFUSED"), "field CONNECTION_REFUSED");
        check(hasField(c, "CRYPTO_BUFFER_EXCEEDED"), "field CRYPTO_BUFFER_EXCEEDED");
        check(hasField(c, "CRYPTO_ERROR"), "field CRYPTO_ERROR");
        check(hasField(c, "FINAL_SIZE_ERROR"), "field FINAL_SIZE_ERROR");
        check(hasField(c, "FLOW_CONTROL_ERROR"), "field FLOW_CONTROL_ERROR");
        check(hasField(c, "FRAME_ENCODING_ERROR"), "field FRAME_ENCODING_ERROR");
        check(hasField(c, "INTERNAL_ERROR"), "field INTERNAL_ERROR");
        check(hasField(c, "INVALID_TOKEN"), "field INVALID_TOKEN");
        check(hasField(c, "KEY_UPDATE_ERROR"), "field KEY_UPDATE_ERROR");
        check(hasField(c, "NO_ERROR"), "field NO_ERROR");
        check(hasField(c, "NO_VIABLE_PATH"), "field NO_VIABLE_PATH");
        check(hasField(c, "PROTOCOL_VIOLATION"), "field PROTOCOL_VIOLATION");
        check(hasField(c, "STREAM_LIMIT_ERROR"), "field STREAM_LIMIT_ERROR");
        check(hasField(c, "STREAM_STATE_ERROR"), "field STREAM_STATE_ERROR");
        check(hasField(c, "TRANSPORT_PARAMETER_ERROR"), "field TRANSPORT_PARAMETER_ERROR");
        check(hasField(c, "VERSION_NEGOTIATION_ERROR"), "field VERSION_NEGOTIATION_ERROR");
        check(hasField(c, "from"), "field from");
        check(hasField(c, "to"), "field to");
        check(hasEnumConstant(c, "AEAD_LIMIT_REACHED"), "enum AEAD_LIMIT_REACHED");
        check(hasEnumConstant(c, "APPLICATION_ERROR"), "enum APPLICATION_ERROR");
        check(hasEnumConstant(c, "CONNECTION_ID_LIMIT_ERROR"), "enum CONNECTION_ID_LIMIT_ERROR");
        check(hasEnumConstant(c, "CONNECTION_REFUSED"), "enum CONNECTION_REFUSED");
        check(hasEnumConstant(c, "CRYPTO_BUFFER_EXCEEDED"), "enum CRYPTO_BUFFER_EXCEEDED");
        check(hasEnumConstant(c, "CRYPTO_ERROR"), "enum CRYPTO_ERROR");
        check(hasEnumConstant(c, "FINAL_SIZE_ERROR"), "enum FINAL_SIZE_ERROR");
        check(hasEnumConstant(c, "FLOW_CONTROL_ERROR"), "enum FLOW_CONTROL_ERROR");
        check(hasEnumConstant(c, "FRAME_ENCODING_ERROR"), "enum FRAME_ENCODING_ERROR");
        check(hasEnumConstant(c, "INTERNAL_ERROR"), "enum INTERNAL_ERROR");
        check(hasEnumConstant(c, "INVALID_TOKEN"), "enum INVALID_TOKEN");
        check(hasEnumConstant(c, "KEY_UPDATE_ERROR"), "enum KEY_UPDATE_ERROR");
        check(hasEnumConstant(c, "NO_ERROR"), "enum NO_ERROR");
        check(hasEnumConstant(c, "NO_VIABLE_PATH"), "enum NO_VIABLE_PATH");
        check(hasEnumConstant(c, "PROTOCOL_VIOLATION"), "enum PROTOCOL_VIOLATION");
        check(hasEnumConstant(c, "STREAM_LIMIT_ERROR"), "enum STREAM_LIMIT_ERROR");
        check(hasEnumConstant(c, "STREAM_STATE_ERROR"), "enum STREAM_STATE_ERROR");
        check(hasEnumConstant(c, "TRANSPORT_PARAMETER_ERROR"), "enum TRANSPORT_PARAMETER_ERROR");
        check(hasEnumConstant(c, "VERSION_NEGOTIATION_ERROR"), "enum VERSION_NEGOTIATION_ERROR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
