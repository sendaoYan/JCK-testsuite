/*
 * @test
 * @summary java.lang.classfile.attribute.SourceFileAttribute conformance: behavioral.
 * @run main SourceFileAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class SourceFileAttributeTest {
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
        SourceFileAttribute a = SourceFileAttribute.of("X.java");
        check("X.java".equals(a.sourceFile().stringValue()), "sourceFile");
        check("SourceFile".equals(a.attributeName().stringValue()), "attributeName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
