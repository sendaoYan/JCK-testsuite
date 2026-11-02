/*
 * @test
 * @summary java.lang.classfile.attribute.SourceDebugExtensionAttribute conformance: behavioral.
 * @run main SourceDebugExtensionAttributeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class SourceDebugExtensionAttributeTest {
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
        byte[] c = new byte[]{1, 2, 3};
        SourceDebugExtensionAttribute a = SourceDebugExtensionAttribute.of(c);
        check(java.util.Arrays.equals(c, a.contents()), "contents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
