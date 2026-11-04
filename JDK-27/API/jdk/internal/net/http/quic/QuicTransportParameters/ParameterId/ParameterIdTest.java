/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicTransportParameters.ParameterId conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main ParameterIdTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ParameterIdTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicTransportParameters$ParameterId", false, ParameterIdTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicTransportParameters$ParameterId"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.quic.QuicTransportParameters$TransportParameterId"), "iface:jdk.internal.net.http.quic.QuicTransportParameters$TransportParameterId");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("idx"), "method:idx");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("valueOf"), "method:valueOf");
        check(methods.contains("values"), "method:values");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ack_delay_exponent"), "field:ack_delay_exponent");
        check(fields.contains("active_connection_id_limit"), "field:active_connection_id_limit");
        check(fields.contains("disable_active_migration"), "field:disable_active_migration");
        check(fields.contains("initial_max_data"), "field:initial_max_data");
        check(fields.contains("initial_max_stream_data_bidi_local"), "field:initial_max_stream_data_bidi_local");
        check(fields.contains("initial_max_stream_data_bidi_remote"), "field:initial_max_stream_data_bidi_remote");
        check(fields.contains("initial_max_stream_data_uni"), "field:initial_max_stream_data_uni");
        check(fields.contains("initial_max_streams_bidi"), "field:initial_max_streams_bidi");
        check(fields.contains("initial_max_streams_uni"), "field:initial_max_streams_uni");
        check(fields.contains("initial_source_connection_id"), "field:initial_source_connection_id");
        check(fields.contains("max_ack_delay"), "field:max_ack_delay");
        check(fields.contains("max_idle_timeout"), "field:max_idle_timeout");
        check(fields.contains("max_udp_payload_size"), "field:max_udp_payload_size");
        check(fields.contains("original_destination_connection_id"), "field:original_destination_connection_id");
        check(fields.contains("preferred_address"), "field:preferred_address");
        check(fields.contains("retry_source_connection_id"), "field:retry_source_connection_id");
        check(fields.contains("stateless_reset_token"), "field:stateless_reset_token");
        check(fields.contains("version_information"), "field:version_information");
        Set<String> enumConsts = new HashSet<>();
        for (Field f : c.getDeclaredFields()) if (f.isEnumConstant()) enumConsts.add(f.getName());
        check(enumConsts.contains("ack_delay_exponent"), "enum-const:ack_delay_exponent");
        check(enumConsts.contains("active_connection_id_limit"), "enum-const:active_connection_id_limit");
        check(enumConsts.contains("disable_active_migration"), "enum-const:disable_active_migration");
        check(enumConsts.contains("initial_max_data"), "enum-const:initial_max_data");
        check(enumConsts.contains("initial_max_stream_data_bidi_local"), "enum-const:initial_max_stream_data_bidi_local");
        check(enumConsts.contains("initial_max_stream_data_bidi_remote"), "enum-const:initial_max_stream_data_bidi_remote");
        check(enumConsts.contains("initial_max_stream_data_uni"), "enum-const:initial_max_stream_data_uni");
        check(enumConsts.contains("initial_max_streams_bidi"), "enum-const:initial_max_streams_bidi");
        check(enumConsts.contains("initial_max_streams_uni"), "enum-const:initial_max_streams_uni");
        check(enumConsts.contains("initial_source_connection_id"), "enum-const:initial_source_connection_id");
        check(enumConsts.contains("max_ack_delay"), "enum-const:max_ack_delay");
        check(enumConsts.contains("max_idle_timeout"), "enum-const:max_idle_timeout");
        check(enumConsts.contains("max_udp_payload_size"), "enum-const:max_udp_payload_size");
        check(enumConsts.contains("original_destination_connection_id"), "enum-const:original_destination_connection_id");
        check(enumConsts.contains("preferred_address"), "enum-const:preferred_address");
        check(enumConsts.contains("retry_source_connection_id"), "enum-const:retry_source_connection_id");
        check(enumConsts.contains("stateless_reset_token"), "enum-const:stateless_reset_token");
        check(enumConsts.contains("version_information"), "enum-const:version_information");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
