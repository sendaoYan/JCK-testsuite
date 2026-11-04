/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.Base64 conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils:+open
 * @run main Base64Test
 */
import java.lang.reflect.*;
import java.util.*;
public class Base64Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.Base64", false, Base64Test.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.Base64"), "name");
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
        check(methods.contains("decode"), "method:decode");
        check(methods.contains("decodeBigIntegerFromElement"), "method:decodeBigIntegerFromElement");
        check(methods.contains("decodeBigIntegerFromText"), "method:decodeBigIntegerFromText");
        check(methods.contains("decodeInternal"), "method:decodeInternal");
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("encodeToElement"), "method:encodeToElement");
        check(methods.contains("fillElementWithBigInteger"), "method:fillElementWithBigInteger");
        check(methods.contains("getBytesInternal"), "method:getBytesInternal");
        check(methods.contains("isPad"), "method:isPad");
        check(methods.contains("isWhiteSpace"), "method:isWhiteSpace");
        check(methods.contains("removeWhiteSpace"), "method:removeWhiteSpace");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("BASE64DEFAULTLENGTH"), "field:BASE64DEFAULTLENGTH");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
