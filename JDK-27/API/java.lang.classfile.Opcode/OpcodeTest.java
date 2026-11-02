/*
 * @test
 * @summary java.lang.classfile.Opcode conformance: behavioral.
 * @run main OpcodeTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.reflect.*;
import java.util.*;

public class OpcodeTest {
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
        check(Opcode.class.isEnum(), "Opcode is an enum");
        check(Opcode.values().length == 213, "213 opcode constants");
        check(Opcode.NOP.bytecode() == 0, "NOP bytecode");
        check(Opcode.ICONST_0.bytecode() == 3, "ICONST_0 bytecode");
        check(Opcode.RETURN.bytecode() == 177, "RETURN bytecode");
        check(Opcode.RETURN.sizeIfFixed() == 1, "RETURN sizeIfFixed");
        check(Opcode.LOOKUPSWITCH.sizeIfFixed() == -1, "LOOKUPSWITCH variable size");
        check(Opcode.RETURN.kind() == Opcode.Kind.RETURN, "RETURN kind");
        check(Opcode.ICONST_0.kind() == Opcode.Kind.CONSTANT, "ICONST_0 kind");
        check(!Opcode.ILOAD.isWide() && Opcode.ILOAD_W.isWide(), "isWide");
        check(Opcode.valueOf("ICONST_0") == Opcode.ICONST_0, "valueOf");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
