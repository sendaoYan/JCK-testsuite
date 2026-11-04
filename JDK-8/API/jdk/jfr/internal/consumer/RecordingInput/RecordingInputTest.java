/*
 * @test
 * @summary jdk.jfr.internal.consumer.RecordingInput conformance: type shape via reflection (headless-safe).
 * @run main RecordingInputTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class RecordingInputTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.jfr.internal.consumer.RecordingInput", false, RecordingInputTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasInterface(c, "java.io.DataInput"), "implements java.io.DataInput");
        check(hasInterface(c, "java.lang.AutoCloseable"), "implements java.lang.AutoCloseable");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "position"), "declares position");
        check(hasMethod(c, "readBoolean"), "declares readBoolean");
        check(hasMethod(c, "readByte"), "declares readByte");
        check(hasMethod(c, "readChar"), "declares readChar");
        check(hasMethod(c, "readDouble"), "declares readDouble");
        check(hasMethod(c, "readEncodedString"), "declares readEncodedString");
        check(hasMethod(c, "readFloat"), "declares readFloat");
        check(hasField(c, "STRING_ENCODING_CHAR_ARRAY"), "declares field STRING_ENCODING_CHAR_ARRAY");
        check(hasField(c, "STRING_ENCODING_CONSTANT_POOL"), "declares field STRING_ENCODING_CONSTANT_POOL");
        check(hasField(c, "STRING_ENCODING_EMPTY_STRING"), "declares field STRING_ENCODING_EMPTY_STRING");
        check(hasField(c, "STRING_ENCODING_LATIN1_BYTE_ARRAY"), "declares field STRING_ENCODING_LATIN1_BYTE_ARRAY");
        check(hasField(c, "STRING_ENCODING_NULL"), "declares field STRING_ENCODING_NULL");
        check(hasField(c, "STRING_ENCODING_UTF8_BYTE_ARRAY"), "declares field STRING_ENCODING_UTF8_BYTE_ARRAY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
