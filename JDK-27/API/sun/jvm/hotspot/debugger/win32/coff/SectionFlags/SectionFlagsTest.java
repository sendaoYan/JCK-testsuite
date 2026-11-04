/*
 * @test
 * @summary sun.jvm.hotspot.debugger.win32.coff.SectionFlags conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.debugger.win32.coff:+open
 * @run main SectionFlagsTest
 */
import java.lang.reflect.*;

public class SectionFlagsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.debugger.win32.coff.SectionFlags", false, SectionFlagsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasField(c, "IMAGE_SCN_TYPE_REG"), "field IMAGE_SCN_TYPE_REG");
        check(hasField(c, "IMAGE_SCN_TYPE_DSECT"), "field IMAGE_SCN_TYPE_DSECT");
        check(hasField(c, "IMAGE_SCN_TYPE_NOLOAD"), "field IMAGE_SCN_TYPE_NOLOAD");
        check(hasField(c, "IMAGE_SCN_TYPE_GROUP"), "field IMAGE_SCN_TYPE_GROUP");
        check(hasField(c, "IMAGE_SCN_TYPE_NO_PAD"), "field IMAGE_SCN_TYPE_NO_PAD");
        check(hasField(c, "IMAGE_SCN_TYPE_COPY"), "field IMAGE_SCN_TYPE_COPY");
        check(hasField(c, "IMAGE_SCN_CNT_CODE"), "field IMAGE_SCN_CNT_CODE");
        check(hasField(c, "IMAGE_SCN_CNT_INITIALIZED_DATA"), "field IMAGE_SCN_CNT_INITIALIZED_DATA");
        check(hasField(c, "IMAGE_SCN_CNT_UNINITIALIZED_DATA"), "field IMAGE_SCN_CNT_UNINITIALIZED_DATA");
        check(hasField(c, "IMAGE_SCN_LNK_OTHER"), "field IMAGE_SCN_LNK_OTHER");
        check(hasField(c, "IMAGE_SCN_LNK_INFO"), "field IMAGE_SCN_LNK_INFO");
        check(hasField(c, "IMAGE_SCN_TYPE_OVER"), "field IMAGE_SCN_TYPE_OVER");
        check(hasField(c, "IMAGE_SCN_LNK_REMOVE"), "field IMAGE_SCN_LNK_REMOVE");
        check(hasField(c, "IMAGE_SCN_LNK_COMDAT"), "field IMAGE_SCN_LNK_COMDAT");
        check(hasField(c, "IMAGE_SCN_MEM_FARDATA"), "field IMAGE_SCN_MEM_FARDATA");
        check(hasField(c, "IMAGE_SCN_MEM_PURGEABLE"), "field IMAGE_SCN_MEM_PURGEABLE");
        check(hasField(c, "IMAGE_SCN_MEM_16BIT"), "field IMAGE_SCN_MEM_16BIT");
        check(hasField(c, "IMAGE_SCN_MEM_LOCKED"), "field IMAGE_SCN_MEM_LOCKED");
        check(hasField(c, "IMAGE_SCN_MEM_PRELOAD"), "field IMAGE_SCN_MEM_PRELOAD");
        check(hasField(c, "IMAGE_SCN_ALIGN_1BYTES"), "field IMAGE_SCN_ALIGN_1BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_2BYTES"), "field IMAGE_SCN_ALIGN_2BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_4BYTES"), "field IMAGE_SCN_ALIGN_4BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_8BYTES"), "field IMAGE_SCN_ALIGN_8BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_16BYTES"), "field IMAGE_SCN_ALIGN_16BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_32BYTES"), "field IMAGE_SCN_ALIGN_32BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_64BYTES"), "field IMAGE_SCN_ALIGN_64BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_128BYTES"), "field IMAGE_SCN_ALIGN_128BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_256BYTES"), "field IMAGE_SCN_ALIGN_256BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_512BYTES"), "field IMAGE_SCN_ALIGN_512BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_1024BYTES"), "field IMAGE_SCN_ALIGN_1024BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_2048BYTES"), "field IMAGE_SCN_ALIGN_2048BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_4096BYTES"), "field IMAGE_SCN_ALIGN_4096BYTES");
        check(hasField(c, "IMAGE_SCN_ALIGN_8192BYTES"), "field IMAGE_SCN_ALIGN_8192BYTES");
        check(hasField(c, "IMAGE_SCN_LNK_NRELOC_OVFL"), "field IMAGE_SCN_LNK_NRELOC_OVFL");
        check(hasField(c, "IMAGE_SCN_MEM_DISCARDABLE"), "field IMAGE_SCN_MEM_DISCARDABLE");
        check(hasField(c, "IMAGE_SCN_MEM_NOT_CACHED"), "field IMAGE_SCN_MEM_NOT_CACHED");
        check(hasField(c, "IMAGE_SCN_MEM_NOT_PAGED"), "field IMAGE_SCN_MEM_NOT_PAGED");
        check(hasField(c, "IMAGE_SCN_MEM_SHARED"), "field IMAGE_SCN_MEM_SHARED");
        check(hasField(c, "IMAGE_SCN_MEM_EXECUTE"), "field IMAGE_SCN_MEM_EXECUTE");
        check(hasField(c, "IMAGE_SCN_MEM_READ"), "field IMAGE_SCN_MEM_READ");
        check(hasField(c, "IMAGE_SCN_MEM_WRITE"), "field IMAGE_SCN_MEM_WRITE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
