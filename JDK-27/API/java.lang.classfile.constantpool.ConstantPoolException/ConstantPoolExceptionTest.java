/*
 * @test
 * @summary java.lang.classfile.constantpool.ConstantPoolException conformance: behavioral.
 * @run main ConstantPoolExceptionTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class ConstantPoolExceptionTest {
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
        ConstantPoolException e1 = new ConstantPoolException();
        check(e1 instanceof IllegalArgumentException, "extends IllegalArgumentException");
        ConstantPoolException e2 = new ConstantPoolException("msg");
        check("msg".equals(e2.getMessage()), "message");
        ConstantPoolException e3 = new ConstantPoolException(new RuntimeException("cause"));
        check(e3.getCause() instanceof RuntimeException, "cause");
        ConstantPoolException e4 = new ConstantPoolException("msg", new RuntimeException());
        check("msg".equals(e4.getMessage()) && e4.getCause() != null, "message+cause");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
