/*
 * @test
 * @summary java.lang.classfile.AttributeMapper conformance: behavioral.
 * @run main AttributeMapperTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class AttributeMapperTest {
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
        AttributeMapper<SignatureAttribute> m = Attributes.signature();
        check("Signature".equals(m.name()), "name");
        check(m.stability() != null, "stability");
        check(!m.allowMultiple(), "allowMultiple");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
