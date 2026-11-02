/*
 * @test
 * @summary java.lang.classfile.Interfaces conformance: behavioral.
 * @run main InterfacesTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class InterfacesTest {
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
        Interfaces i = Interfaces.ofSymbols(ClassDesc.of("java.io.Serializable"), ClassDesc.of("java.lang.Comparable"));
        check(i.interfaces().size() == 2, "two interfaces");
        check("java/io/Serializable".equals(i.interfaces().get(0).asInternalName()), "first interface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
