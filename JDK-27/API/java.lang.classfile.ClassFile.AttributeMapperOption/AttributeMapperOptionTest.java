/*
 * @test
 * @summary java.lang.classfile.ClassFile.AttributeMapperOption conformance: behavioral.
 * @run main AttributeMapperOptionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class AttributeMapperOptionTest {
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
        ClassFile.AttributeMapperOption o = ClassFile.AttributeMapperOption.of(ue -> Attributes.sourceFile());
        check(o.attributeMapper() != null, "attributeMapper");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
