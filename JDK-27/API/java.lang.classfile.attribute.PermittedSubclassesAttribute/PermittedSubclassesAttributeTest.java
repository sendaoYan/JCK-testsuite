/*
 * @test
 * @summary java.lang.classfile.attribute.PermittedSubclassesAttribute conformance: behavioral.
 * @run main PermittedSubclassesAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class PermittedSubclassesAttributeTest {
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
        PermittedSubclassesAttribute a = PermittedSubclassesAttribute.ofSymbols(ClassDesc.of("pkg.S"));
        check(a.permittedSubclasses().size() == 1, "permittedSubclasses");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
