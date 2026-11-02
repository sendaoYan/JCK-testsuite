/*
 * @test
 * @summary sun.nio.cs.StreamEncoder conformance: internal package reflection.
 * @modules java.base/sun.nio.cs:+open
 * @run main StreamEncoderTest
 */
public class StreamEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.StreamEncoder", false, StreamEncoderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.Writer"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "flush"), "method flush");
        check(hasMethod(c, "flushBuffer"), "method flushBuffer");
        check(hasMethod(c, "forOutputStreamWriter"), "method forOutputStreamWriter");
        check(hasMethod(c, "getEncoding"), "method getEncoding");
        check(hasMethod(c, "write"), "method write");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "INITIAL_BYTE_BUFFER_CAPACITY"), "field INITIAL_BYTE_BUFFER_CAPACITY");
        check(hasField(c, "MAX_BYTE_BUFFER_CAPACITY"), "field MAX_BYTE_BUFFER_CAPACITY");
        check(hasField(c, "bb"), "field bb");
        check(hasField(c, "closed"), "field closed");
        check(hasField(c, "cs"), "field cs");
        check(hasField(c, "encoder"), "field encoder");
        check(hasField(c, "haveLeftoverChar"), "field haveLeftoverChar");
        check(hasField(c, "lcb"), "field lcb");
        check(hasField(c, "leftoverChar"), "field leftoverChar");
        check(hasField(c, "maxBufferCapacity"), "field maxBufferCapacity");
        check(hasField(c, "out"), "field out");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
