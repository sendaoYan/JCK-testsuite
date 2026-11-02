/*
 * @test
 * @summary java.lang.classfile.attribute.LineNumberTableAttribute conformance: behavioral.
 * @run main LineNumberTableAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class LineNumberTableAttributeTest {
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
        LineNumberTableAttribute a = LineNumberTableAttribute.of(List.of(LineNumberInfo.of(0, 5)));
        check(a.lineNumbers().size() == 1 && a.lineNumbers().get(0).lineNumber() == 5, "lineNumbers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
