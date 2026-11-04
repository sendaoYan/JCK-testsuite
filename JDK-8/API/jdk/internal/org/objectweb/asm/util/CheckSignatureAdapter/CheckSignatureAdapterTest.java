/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.util.CheckSignatureAdapter conformance: type shape via reflection (headless-safe).
 * @run main CheckSignatureAdapterTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class CheckSignatureAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.util.CheckSignatureAdapter", false, CheckSignatureAdapterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("jdk.internal.org.objectweb.asm.signature.SignatureVisitor"), "superclass jdk.internal.org.objectweb.asm.signature.SignatureVisitor");
        check(hasMethod(c, "visitArrayType"), "declares visitArrayType");
        check(hasMethod(c, "visitBaseType"), "declares visitBaseType");
        check(hasMethod(c, "visitClassBound"), "declares visitClassBound");
        check(hasMethod(c, "visitClassType"), "declares visitClassType");
        check(hasMethod(c, "visitEnd"), "declares visitEnd");
        check(hasMethod(c, "visitExceptionType"), "declares visitExceptionType");
        check(hasMethod(c, "visitFormalTypeParameter"), "declares visitFormalTypeParameter");
        check(hasMethod(c, "visitInnerClassType"), "declares visitInnerClassType");
        check(hasField(c, "CLASS_SIGNATURE"), "declares field CLASS_SIGNATURE");
        check(hasField(c, "METHOD_SIGNATURE"), "declares field METHOD_SIGNATURE");
        check(hasField(c, "TYPE_SIGNATURE"), "declares field TYPE_SIGNATURE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
