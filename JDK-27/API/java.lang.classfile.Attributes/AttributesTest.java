/*
 * @test
 * @summary java.lang.classfile.Attributes conformance: behavioral.
 * @run main AttributesTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class AttributesTest {
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
        check("Code".equals(Attributes.NAME_CODE), "NAME_CODE");
        check("Signature".equals(Attributes.NAME_SIGNATURE), "NAME_SIGNATURE");
        check(Attributes.signature() != null && Attributes.code() != null && Attributes.module() != null, "mappers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
