/*
 * @test
 * @summary JVMS 4.9 (with 6.5 if_icmp<cond>): every conditional branch target
 *          must be within the code array, and the s2 offset is signed, so the
 *          boundary values are +32767 (forward) and -32768 (backward).  Each of
 *          the six if_icmp* opcodes is emitted with a forward jump at the exact
 *          +32767 s2 maximum (both the taken path landing past the ~32 KB nop
 *          span and the fall-through path are executed), and if_icmpne is also
 *          emitted with a backward jump of exactly -32768 in a loop that runs
 *          to completion.  All methods verify (frames supplied) and execute.
 * @run main CondBranchOffsetTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class CondBranchOffsetTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    static void u1(DataOutputStream d, int v) throws Exception { d.writeByte(v); }
    static void u2(DataOutputStream d, int v) throws Exception { d.writeShort(v); }
    static void u4(DataOutputStream d, long v) throws Exception { d.writeInt((int) v); }
    static void utf8(DataOutputStream d, String s) throws Exception {
        u1(d, 1); byte[] b = s.getBytes("UTF-8"); u2(d, b.length); d.write(b);
    }

    static Class<?> load(String name, byte[] b) throws Exception {
        Path dir = Files.createTempDirectory("cls");
        Files.write(dir.resolve(name + ".class"), b);
        URLClassLoader cl = new URLClassLoader(new URL[]{dir.toUri().toURL()}, null);
        return Class.forName(name, true, cl);
    }

    // CP: 1,2=this, 3,4=Object, 5=m, 6=descriptor, 7=Code, 8=StackMapTable
    static void header(DataOutputStream d, String cls, String desc) throws Exception {
        d.writeInt(0xCAFEBABE); u2(d, 0); u2(d, 52);
        u2(d, 9);
        u1(d, 7); u2(d, 2); utf8(d, cls); u1(d, 7); u2(d, 4); utf8(d, "java/lang/Object");
        utf8(d, "m"); utf8(d, desc); utf8(d, "Code"); utf8(d, "StackMapTable");
        u2(d, 0x0021); u2(d, 1); u2(d, 3); u2(d, 0);
        u2(d, 0); u2(d, 1);
        u2(d, 0x0009); u2(d, 5); u2(d, 6); u2(d, 1);          // public static m
    }

    // full_frame with the given ITEM_* locals/stack (1 = Integer)
    static void frame(DataOutputStream d, int delta, int[] locals, int[] stack) throws Exception {
        u1(d, 255); u2(d, delta); u2(d, locals.length);
        for (int t : locals) u1(d, t);
        u2(d, stack.length);
        for (int t : stack) u1(d, t);
    }

    // if_icmp<opcode> at the exact +32767 s2 maximum forward offset:
    //   0: iload_0; 1: iload_1; 2: if_icmp<cond> +32767 -> 32769
    //   5: iconst_0; 6: ireturn            (fall-through -> 0)
    //   [nop] 7..32768
    //   32769: iconst_1; 32770: ireturn    (taken -> 1)
    static byte[] condForward(String cls, int opcode) throws Exception {
        int target = 32769, len = target + 2;   // 32771
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        header(d, cls, "(II)I");
        u2(d, 7); u4(d, len + 47); u2(d, 2); u2(d, 2);
        u4(d, len);
        u1(d, 0x1A);                             // 0: iload_0
        u1(d, 0x1B);                             // 1: iload_1
        u1(d, opcode);                           // 2: if_icmp<cond>
        u2(d, 32767);                            // 3-4: +32767
        u1(d, 0x03);                             // 5: iconst_0
        u1(d, 0xAC);                             // 6: ireturn
        for (int i = 7; i < target; i++) u1(d, 0x00);   // 7..32768 nop
        u1(d, 0x04);                             // 32769: iconst_1
        u1(d, 0xAC);                             // 32770: ireturn
        u2(d, 0); u2(d, 1);
        u2(d, 8); u4(d, 29); u2(d, 3);
        frame(d, 5, new int[]{1, 1}, new int[0]);        // 5: fall-through
        frame(d, 1, new int[]{1, 1}, new int[0]);        // 7: dead block after ireturn
        frame(d, target - 7 - 1, new int[]{1, 1}, new int[0]); // 32769: target
        u2(d, 0);
        return o.toByteArray();
    }

    // if_icmpne at the exact -32768 s2 minimum backward offset, in a loop that
    // executes the back edge once and then falls through:
    //   0: iconst_0; 1: istore_0; 2: iconst_2; 3: istore_1  (counter=0, limit=2)
    //   4: iinc 0,1                       (loop head == branch target)
    //   [nop] 7..32769
    //   32770: iload_0; 32771: iload_1; 32772: if_icmpne -32768 -> 4
    //   32775: iconst_0; 32776: ireturn   (counter reached limit -> 0)
    static byte[] condBackward(String cls) throws Exception {
        int len = 32777;
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        header(d, cls, "()I");
        u2(d, 7); u4(d, len + 38); u2(d, 2); u2(d, 2);
        u4(d, len);
        u1(d, 0x03);                             // 0: iconst_0
        u1(d, 0x3B);                             // 1: istore_0
        u1(d, 0x05);                             // 2: iconst_2
        u1(d, 0x3C);                             // 3: istore_1
        u1(d, 0x84); u1(d, 0); u1(d, 1);         // 4,5,6: iinc 0,1
        for (int i = 7; i < 32770; i++) u1(d, 0x00);     // 7..32769 nop
        u1(d, 0x1A);                             // 32770: iload_0
        u1(d, 0x1B);                             // 32771: iload_1
        u1(d, 0xA0);                             // 32772: if_icmpne
        u2(d, -32768);                           // 32773-32774: -32768
        u1(d, 0x03);                             // 32775: iconst_0
        u1(d, 0xAC);                             // 32776: ireturn
        u2(d, 0); u2(d, 1);
        u2(d, 8); u4(d, 20); u2(d, 2);
        frame(d, 4, new int[]{1, 1}, new int[0]);
        frame(d, 32775 - 4 - 1, new int[]{1, 1}, new int[0]);
        u2(d, 0);
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        // if_icmpeq, if_icmpne, if_icmplt, if_icmpge, if_icmpgt, if_icmple
        int[] opcodes = {0x9F, 0xA0, 0xA1, 0xA2, 0xA3, 0xA4};
        String[] names = {"IfIcmpeq", "IfIcmpne", "IfIcmplt", "IfIcmpge", "IfIcmpgt", "IfIcmple"};
        // {a,b} that satisfies the condition (taken -> 1) / that does not (fall -> 0)
        int[][] taken   = {{1,1},{1,2},{1,2},{2,1},{2,1},{1,2}};
        int[][] notTaken = {{1,2},{1,1},{2,1},{1,2},{1,2},{2,1}};

        for (int i = 0; i < opcodes.length; i++) {
            Class<?> c = load(names[i], condForward(names[i], opcodes[i]));
            Object t = c.getMethod("m", int.class, int.class)
                    .invoke(null, taken[i][0], taken[i][1]);
            check(((Integer) t).intValue() == 1, names[i] + " forward +32767 taken");
            Object n = c.getMethod("m", int.class, int.class)
                    .invoke(null, notTaken[i][0], notTaken[i][1]);
            check(((Integer) n).intValue() == 0, names[i] + " forward +32767 fall-through");
        }

        load("IfIcmpneBack", condBackward("IfIcmpneBack")).getMethod("m").invoke(null);
        check(true, "if_icmpne backward -32768 loop completes");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
