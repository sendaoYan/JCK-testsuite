/*
 * @test
 * @summary java.lang.classfile.ClassFileVersion conformance: behavioral.
 * @run main ClassFileVersionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ClassFileVersionTest {
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
        ClassFileVersion v = ClassFileVersion.of(71, 0);
        check(v.majorVersion() == 71 && v.minorVersion() == 0, "of(71,0)");
        ClassFileVersion p = ClassFileVersion.of(61, 65535);
        check(p.majorVersion() == 61 && p.minorVersion() == 65535, "preview minor version");
        check(ClassFileVersion.of(52, 0).majorVersion() == 52, "of(52,0)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
