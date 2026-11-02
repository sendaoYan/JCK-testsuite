/*
 * @test
 * @summary sun.net.www.http.ChunkedOutputStream conformance: internal package reflection.
 * @modules java.base/sun.net.www.http:+open
 * @run main ChunkedOutputStreamTest
 */
public class ChunkedOutputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.ChunkedOutputStream", false, ChunkedOutputStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.OutputStream"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "checkError"), "method checkError");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "flush"), "method flush");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "write"), "method write");
        check(hasField(c, "CRLF"), "field CRLF");
        check(hasField(c, "CRLF_SIZE"), "field CRLF_SIZE");
        check(hasField(c, "DEFAULT_CHUNK_SIZE"), "field DEFAULT_CHUNK_SIZE");
        check(hasField(c, "EMPTY_CHUNK_HEADER"), "field EMPTY_CHUNK_HEADER");
        check(hasField(c, "EMPTY_CHUNK_HEADER_SIZE"), "field EMPTY_CHUNK_HEADER_SIZE");
        check(hasField(c, "FOOTER"), "field FOOTER");
        check(hasField(c, "FOOTER_SIZE"), "field FOOTER_SIZE");
        check(hasField(c, "buf"), "field buf");
        check(hasField(c, "completeHeader"), "field completeHeader");
        check(hasField(c, "count"), "field count");
        check(hasField(c, "out"), "field out");
        check(hasField(c, "preferredChunkDataSize"), "field preferredChunkDataSize");
        check(hasField(c, "preferredChunkGrossSize"), "field preferredChunkGrossSize");
        check(hasField(c, "preferredHeaderSize"), "field preferredHeaderSize");
        check(hasField(c, "size"), "field size");
        check(hasField(c, "spaceInCurrentChunk"), "field spaceInCurrentChunk");
        check(hasField(c, "writeLock"), "field writeLock");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
