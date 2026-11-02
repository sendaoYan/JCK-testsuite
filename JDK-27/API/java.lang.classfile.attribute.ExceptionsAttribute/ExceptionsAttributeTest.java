/*
 * @test
 * @summary java.lang.classfile.attribute.ExceptionsAttribute conformance: behavioral.
 * @run main ExceptionsAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ExceptionsAttributeTest {
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
        ExceptionsAttribute a = ExceptionsAttribute.ofSymbols(ClassDesc.of("java.io.IOException"));
        check(a.exceptions().size() == 1, "exceptions");
        check("java/io/IOException".equals(a.exceptions().get(0).asInternalName()), "exception class");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
