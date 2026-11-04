/*
 * @test
 * @summary sun.nio.ch.FileChannelImpl conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main FileChannelImplTest
 */
public class FileChannelImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.FileChannelImpl", false, FileChannelImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.FileChannel"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "force"), "method force");
        check(hasMethod(c, "getMappedBufferPool"), "method getMappedBufferPool");
        check(hasMethod(c, "getSyncMappedBufferPool"), "method getSyncMappedBufferPool");
        check(hasMethod(c, "implCloseChannel"), "method implCloseChannel");
        check(hasMethod(c, "lock"), "method lock");
        check(hasMethod(c, "map"), "method map");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "position"), "method position");
        check(hasMethod(c, "read"), "method read");
        check(hasMethod(c, "setUninterruptible"), "method setUninterruptible");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "transferFrom"), "method transferFrom");
        check(hasMethod(c, "transferTo"), "method transferTo");
        check(hasMethod(c, "truncate"), "method truncate");
        check(hasMethod(c, "tryLock"), "method tryLock");
        check(hasMethod(c, "write"), "method write");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "MAPPED_TRANSFER_SIZE"), "field MAPPED_TRANSFER_SIZE");
        check(hasField(c, "MAPPED_TRANSFER_THRESHOLD"), "field MAPPED_TRANSFER_THRESHOLD");
        check(hasField(c, "MAP_INVALID"), "field MAP_INVALID");
        check(hasField(c, "MAP_PV"), "field MAP_PV");
        check(hasField(c, "MAP_RO"), "field MAP_RO");
        check(hasField(c, "MAP_RW"), "field MAP_RW");
        check(hasField(c, "TRANSFER_SIZE"), "field TRANSFER_SIZE");
        check(hasField(c, "alignment"), "field alignment");
        check(hasField(c, "closer"), "field closer");
        check(hasField(c, "direct"), "field direct");
        check(hasField(c, "fd"), "field fd");
        check(hasField(c, "fdAccess"), "field fdAccess");
        check(hasField(c, "fileLockTable"), "field fileLockTable");
        check(hasField(c, "jfrTracing"), "field jfrTracing");
        check(hasField(c, "nd"), "field nd");
        check(hasField(c, "parent"), "field parent");
        check(hasField(c, "path"), "field path");
        check(hasField(c, "positionLock"), "field positionLock");
        check(hasField(c, "readable"), "field readable");
        check(hasField(c, "sync"), "field sync");
        check(hasField(c, "threads"), "field threads");
        check(hasField(c, "transferFromDirectNotSupported"), "field transferFromDirectNotSupported");
        check(hasField(c, "transferToDirectNotSupported"), "field transferToDirectNotSupported");
        check(hasField(c, "uninterruptible"), "field uninterruptible");
        check(hasField(c, "writable"), "field writable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
