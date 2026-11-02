/*
 * @test
 * @summary java.lang.classfile.constantpool.StringEntry conformance: behavioral.
 * @run main StringEntryTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class StringEntryTest {
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
        ConstantPoolBuilder cpb = ConstantPoolBuilder.of();
        StringEntry e = cpb.stringEntry("s");
        check("s".equals(e.stringValue()), "stringValue");
        check(e.equalsString("s"), "equalsString");
        check("s".equals(e.utf8().stringValue()), "utf8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
