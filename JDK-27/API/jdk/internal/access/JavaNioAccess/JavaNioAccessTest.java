/*
 * @test
 * @summary jdk.internal.access.JavaNioAccess conformance: internal package reflection.
 * @modules java.base/jdk.internal.access:+open
 * @run main JavaNioAccessTest
 */
public class JavaNioAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.access.JavaNioAccess", false, JavaNioAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "acquireSession"), "method acquireSession");
        check(hasMethod(c, "bufferSegment"), "method bufferSegment");
        check(hasMethod(c, "getBufferAddress"), "method getBufferAddress");
        check(hasMethod(c, "getBufferBase"), "method getBufferBase");
        check(hasMethod(c, "getDirectBufferPool"), "method getDirectBufferPool");
        check(hasMethod(c, "hasSession"), "method hasSession");
        check(hasMethod(c, "heapSegment"), "method heapSegment");
        check(hasMethod(c, "isThreadConfined"), "method isThreadConfined");
        check(hasMethod(c, "mappedMemoryUtils"), "method mappedMemoryUtils");
        check(hasMethod(c, "newDirectByteBuffer"), "method newDirectByteBuffer");
        check(hasMethod(c, "newHeapByteBuffer"), "method newHeapByteBuffer");
        check(hasMethod(c, "newMappedByteBuffer"), "method newMappedByteBuffer");
        check(hasMethod(c, "pageSize"), "method pageSize");
        check(hasMethod(c, "releaseSession"), "method releaseSession");
        check(hasMethod(c, "reserveMemory"), "method reserveMemory");
        check(hasMethod(c, "scaleShifts"), "method scaleShifts");
        check(hasMethod(c, "unmapper"), "method unmapper");
        check(hasMethod(c, "unreserveMemory"), "method unreserveMemory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
