/*
 * @test
 * @summary jdk.internal.net.http.qpack.readers.EncoderInstructionsReader.Callback conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack.readers:+open
 * @run main CallbackTest
 */
import java.lang.reflect.*;
import java.util.*;
public class CallbackTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.readers.EncoderInstructionsReader$Callback", false, CallbackTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.readers.EncoderInstructionsReader$Callback"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("onCapacityUpdate"), "method:onCapacityUpdate");
        check(methods.contains("onDuplicate"), "method:onDuplicate");
        check(methods.contains("onInsert"), "method:onInsert");
        check(methods.contains("onInsertIndexedName"), "method:onInsertIndexedName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
