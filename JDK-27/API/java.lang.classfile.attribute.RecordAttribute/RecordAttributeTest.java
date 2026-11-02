/*
 * @test
 * @summary java.lang.classfile.attribute.RecordAttribute conformance: behavioral.
 * @run main RecordAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class RecordAttributeTest {
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
        RecordComponentInfo i = RecordComponentInfo.of("c", ConstantDescs.CD_int);
        RecordAttribute a = RecordAttribute.of(i);
        check(a.components().size() == 1, "components");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
