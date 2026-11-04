/*
 * @test
 * @summary jdk.internal.net.http.quic.VariableLengthEncoder conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main VariableLengthEncoderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class VariableLengthEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.VariableLengthEncoder", false, VariableLengthEncoderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.VariableLengthEncoder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("decode"), "method:decode");
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("getEncodedSize"), "method:getEncodedSize");
        check(methods.contains("getVariableLengthPrefix"), "method:getVariableLengthPrefix");
        check(methods.contains("peekEncodedValue"), "method:peekEncodedValue");
        check(methods.contains("peekEncodedValueSize"), "method:peekEncodedValueSize");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("MAX_ENCODED_INTEGER"), "field:MAX_ENCODED_INTEGER");
        check(fields.contains("MAX_INTEGER_LENGTH"), "field:MAX_INTEGER_LENGTH");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
