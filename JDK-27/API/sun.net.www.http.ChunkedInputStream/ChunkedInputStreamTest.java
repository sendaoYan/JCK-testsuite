/*
 * @test
 * @summary sun.net.www.http.ChunkedInputStream conformance: internal package reflection.
 * @modules java.base/sun.net.www.http:+open
 * @run main ChunkedInputStreamTest
 */
public class ChunkedInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.www.http.ChunkedInputStream", false, ChunkedInputStreamTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.InputStream"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.net.www.http.Hurryable"), "interface sun.net.www.http.Hurryable");
        check(hasMethod(c, "available"), "method available");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "hurry"), "method hurry");
        check(hasMethod(c, "read"), "method read");
        check(hasField(c, "MAX_CHUNK_HEADER_SIZE"), "field MAX_CHUNK_HEADER_SIZE");
        check(hasField(c, "STATE_AWAITING_CHUNK_EOL"), "field STATE_AWAITING_CHUNK_EOL");
        check(hasField(c, "STATE_AWAITING_CHUNK_HEADER"), "field STATE_AWAITING_CHUNK_HEADER");
        check(hasField(c, "STATE_AWAITING_TRAILERS"), "field STATE_AWAITING_TRAILERS");
        check(hasField(c, "STATE_DONE"), "field STATE_DONE");
        check(hasField(c, "STATE_READING_CHUNK"), "field STATE_READING_CHUNK");
        check(hasField(c, "chunkCount"), "field chunkCount");
        check(hasField(c, "chunkData"), "field chunkData");
        check(hasField(c, "chunkPos"), "field chunkPos");
        check(hasField(c, "chunkRead"), "field chunkRead");
        check(hasField(c, "chunkSize"), "field chunkSize");
        check(hasField(c, "closed"), "field closed");
        check(hasField(c, "error"), "field error");
        check(hasField(c, "hc"), "field hc");
        check(hasField(c, "in"), "field in");
        check(hasField(c, "rawCount"), "field rawCount");
        check(hasField(c, "rawData"), "field rawData");
        check(hasField(c, "rawPos"), "field rawPos");
        check(hasField(c, "readLock"), "field readLock");
        check(hasField(c, "responses"), "field responses");
        check(hasField(c, "state"), "field state");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
