/*
 * @test
 * @summary java.lang.classfile.ClassFile conformance: behavioral.
 * @run main ClassFileTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassFileTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        ClassFile cf = ClassFile.of();
        check(ClassFile.MAGIC_NUMBER == 0xCAFEBABE, "MAGIC_NUMBER is 0xCAFEBABE");
        check(ClassFile.ACC_PUBLIC == 0x0001, "ACC_PUBLIC");
        check(ClassFile.ACC_STATIC == 0x0008, "ACC_STATIC");
        check(ClassFile.ACC_PRIVATE == 0x0002, "ACC_PRIVATE");
        check(ClassFile.ACC_FINAL == 0x0010, "ACC_FINAL");
        check(ClassFile.JAVA_1_VERSION == 45, "JAVA_1_VERSION");
        check(ClassFile.JAVA_27_VERSION == 71, "JAVA_27_VERSION");
        check(ClassFile.PREVIEW_MINOR_VERSION == 0xFFFF, "PREVIEW_MINOR_VERSION");
        check(ClassFile.latestMajorVersion() == 71, "latestMajorVersion");
        check(ClassFile.latestMinorVersion() == 0, "latestMinorVersion");
        byte[] b = cf.build(ClassDesc.of("pkg.T"), cb -> cb.withSuperclass(ClassDesc.of("java.lang.Object")));
        check(b.length > 8, "build produces bytes");
        check(((b[0] & 0xff) << 24 | (b[1] & 0xff) << 16 | (b[2] & 0xff) << 8 | (b[3] & 0xff)) == 0xCAFEBABE, "magic number in bytes");
        ClassModel cm = cf.parse(b);
        check("pkg/T".equals(cm.thisClass().asInternalName()), "parse round trip thisClass");
        check("java/lang/Object".equals(cm.superclass().get().asInternalName()), "superclass");
        check(cf.verify(cm).isEmpty(), "verify clean");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
