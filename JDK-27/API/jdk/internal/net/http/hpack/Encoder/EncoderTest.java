/*
 * @test
 * @summary jdk.internal.net.http.hpack.Encoder conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.hpack:+open
 * @run main EncoderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class EncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.hpack.Encoder", false, EncoderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.hpack.Encoder"), "name");
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
        check(methods.contains("calculateCapacity"), "method:calculateCapacity");
        check(methods.contains("checkEncoding"), "method:checkEncoding");
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("getHeaderTable"), "method:getHeaderTable");
        check(methods.contains("getMaxCapacity"), "method:getMaxCapacity");
        check(methods.contains("header"), "method:header");
        check(methods.contains("indexed"), "method:indexed");
        check(methods.contains("isHuffmanBetterFor"), "method:isHuffmanBetterFor");
        check(methods.contains("literal"), "method:literal");
        check(methods.contains("literalNeverIndexed"), "method:literalNeverIndexed");
        check(methods.contains("literalWithIndexing"), "method:literalWithIndexing");
        check(methods.contains("logger"), "method:logger");
        check(methods.contains("setMaxCapacity"), "method:setMaxCapacity");
        check(methods.contains("sizeUpdate"), "method:sizeUpdate");
        check(methods.contains("tableIndexOf"), "method:tableIndexOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
