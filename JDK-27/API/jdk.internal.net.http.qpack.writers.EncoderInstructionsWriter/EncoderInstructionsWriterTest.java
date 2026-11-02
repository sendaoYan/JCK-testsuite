/*
 * @test
 * @summary jdk.internal.net.http.qpack.writers.EncoderInstructionsWriter conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack.writers:+open
 * @run main EncoderInstructionsWriterTest
 */
import java.lang.reflect.*;
import java.util.*;
public class EncoderInstructionsWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.writers.EncoderInstructionsWriter", false, EncoderInstructionsWriterTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.writers.EncoderInstructionsWriter"), "name");
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
        check(methods.contains("configureForEntryDuplication"), "method:configureForEntryDuplication");
        check(methods.contains("configureForEntryInsertion"), "method:configureForEntryInsertion");
        check(methods.contains("configureForTableCapacityUpdate"), "method:configureForTableCapacityUpdate");
        check(methods.contains("write"), "method:write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
