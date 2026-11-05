/*
 * @test
 * @summary jdk.jshell.spi.ExecutionControl.ClassBytecodes conformance: class bytecodes record.
 * @run main ClassBytecodesTest
 */

import jdk.jshell.execution.*;
import jdk.jshell.spi.*;
import jdk.jshell.spi.ExecutionControl.*;
import java.util.*;

public class ClassBytecodesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- deep contract: serialization round-trip and array reference semantics ----
    static void testDeepSemantics() throws Exception {
        ClassBytecodes cb = new ClassBytecodes("com.Foo", new byte[]{1, 2, 3});
        // bytecodes() hands out the internal array (no defensive copy)
        byte[] arr = cb.bytecodes();
        arr[0] = 99;
        check(cb.bytecodes()[0] == 99, "bytecodes() shares the array reference");
        check(cb.bytecodes() == arr, "same instance on repeated calls");
        // empty array is legal
        ClassBytecodes empty = new ClassBytecodes("E", new byte[0]);
        check(empty.bytecodes().length == 0, "empty bytecodes allowed");
        // null bytecodes are accepted by the constructor (rejected later at load time)
        check(new ClassBytecodes("N", null).bytecodes() == null, "null bytecodes field kept as-is");
        // serialization round-trip preserves both fields
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oo = new java.io.ObjectOutputStream(bos)) { oo.writeObject(cb); }
        try (java.io.ObjectInputStream oi = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            ClassBytecodes back = (ClassBytecodes) oi.readObject();
            check(back.name().equals("com.Foo"), "name survives serialization");
            check(back.bytecodes().length == 3 && back.bytecodes()[2] == 3, "bytecodes survive serialization");
        }
        // the class pins a fixed serialVersionUID
        check(java.io.ObjectStreamClass.lookup(ClassBytecodes.class).getSerialVersionUID() == 0xC1A55B47EC0DE5L,
              "documented serialVersionUID 0xC1A55B47EC0DE5");
        // no equals/hashCode override: identity semantics for equal-content twins
        ClassBytecodes twin = new ClassBytecodes("com.Foo", new byte[]{99, 2, 3});
        check(!cb.equals(twin), "no value equality: equal-content twins stay distinct");
    }

    public static void main(String[] args) throws Exception {
        ClassBytecodes cb = new ClassBytecodes("com.Foo", new byte[]{1, 2, 3});
        check(cb.name().equals("com.Foo"), "name");
        check(cb.bytecodes().length == 3, "bytecodes");
        check(cb instanceof java.io.Serializable, "serializable");
        testDeepSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
