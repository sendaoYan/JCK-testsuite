/*
 * @test
 * @summary java.lang.classfile.ClassReader conformance: reflection shape checks.
 * @run main ClassReaderTest
 */

import java.lang.reflect.*;

public class ClassReaderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }


    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.ClassReader");
        check(c != null, "java.lang.classfile.ClassReader loads");
        check(c.getName().equals("java.lang.classfile.ClassReader"), "binary name is java.lang.classfile.ClassReader");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassReader is public");
        check(c.isInterface(), "java.lang.classfile.ClassReader is an interface");
        check(declares(c, "classfileLength"), "java.lang.classfile.ClassReader declares classfileLength()");
        check(declares(c, "copyBytesTo"), "java.lang.classfile.ClassReader declares copyBytesTo()");
        check(declares(c, "customAttributes"), "java.lang.classfile.ClassReader declares customAttributes()");
        check(declares(c, "flags"), "java.lang.classfile.ClassReader declares flags()");
        check(declares(c, "readBytes"), "java.lang.classfile.ClassReader declares readBytes()");
        check(declares(c, "readDouble"), "java.lang.classfile.ClassReader declares readDouble()");
        check(declares(c, "readEntry"), "java.lang.classfile.ClassReader declares readEntry()");
        check(declares(c, "readEntryOrNull"), "java.lang.classfile.ClassReader declares readEntryOrNull()");
        check(declares(c, "readFloat"), "java.lang.classfile.ClassReader declares readFloat()");
        check(declares(c, "readInt"), "java.lang.classfile.ClassReader declares readInt()");
        check(declares(c, "readLong"), "java.lang.classfile.ClassReader declares readLong()");
        check(declares(c, "readS1"), "java.lang.classfile.ClassReader declares readS1()");
        check(declares(c, "readS2"), "java.lang.classfile.ClassReader declares readS2()");
        check(declares(c, "readU1"), "java.lang.classfile.ClassReader declares readU1()");
        check(declares(c, "readU2"), "java.lang.classfile.ClassReader declares readU2()");
        check(declares(c, "superclassEntry"), "java.lang.classfile.ClassReader declares superclassEntry()");
        check(declares(c, "thisClassEntry"), "java.lang.classfile.ClassReader declares thisClassEntry()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
