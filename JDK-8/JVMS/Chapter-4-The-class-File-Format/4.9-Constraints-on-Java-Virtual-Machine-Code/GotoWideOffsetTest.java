/*
 * @test
 * @summary JVMS 4.9 (with 6.5 goto/goto_w/tableswitch): every branch target
 *          must be within the code array.  goto/if offsets are s2, so offsets
 *          beyond [-32768, 32767] can only be encoded by the wide forms: a
 *          goto at the exact +32767 s2 bound, a forward goto_w over +40000
 *          bytes, a backward goto_w loop of -40007, and a tableswitch whose
 *          match offset is +39999 all verify (frames supplied) and execute.
 * @run main GotoWideOffsetTest
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public class GotoWideOffsetTest {
    static int passed;
    static void check(boolean c, String m) { if (c) passed++; else throw new AssertionError(m); }

    static void u1(java.io.DataOutputStream d, int v) throws Exception { d.writeByte(v); }
    static void u2(java.io.DataOutputStream d, int v) throws Exception { d.writeShort(v); }
    static void u4(java.io.DataOutputStream d, long v) throws Exception { d.writeInt((int) v); }
    static void utf8(java.io.DataOutputStream d, String s) throws Exception {
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

    // The verifier requires a frame at every basic-block start, so each method
    // below emits frames both right after the branch instruction and at its target.

    // 0: goto_w -> 40000; nops; 40000: iconst_1; ireturn   ()I
    static byte[] gotoWForward(String cls) throws Exception {
        int target = 40000, len = target + 2;
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        header(d, cls, "()I");
        u2(d, 7); u4(d, len + 34); u2(d, 1); u2(d, 0);
        u4(d, len);
        u1(d, 0xC8); u4(d, target);
        for (int i = 5; i < target; i++) u1(d, 0x00);
        u1(d, 0x04); u1(d, 0xAC);
        u2(d, 0); u2(d, 1);
        u2(d, 8); u4(d, 16); u2(d, 2);
        frame(d, 5, new int[0], new int[0]);
        frame(d, target - 5 - 1, new int[0], new int[0]);
        u2(d, 0);
        return o.toByteArray();
    }

    // 0: goto +32767 (the s2 maximum); nops; 32767: return   ()V
    static byte[] gotoBoundary(String cls) throws Exception {
        int target = 32767, len = target + 1;
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        header(d, cls, "()V");
        u2(d, 7); u4(d, len + 34); u2(d, 0); u2(d, 0);
        u4(d, len);
        u1(d, 0xA7); u2(d, target);
        for (int i = 3; i < target; i++) u1(d, 0x00);
        u1(d, 0xB1);
        u2(d, 0); u2(d, 1);
        u2(d, 8); u4(d, 16); u2(d, 2);
        frame(d, 3, new int[0], new int[0]);
        frame(d, target - 3 - 1, new int[0], new int[0]);
        u2(d, 0);
        return o.toByteArray();
    }

    // i = 1 (local 0); 40000 nops; iinc 0,-1; iload_0; ifle end;
    // goto_w back to the head (offset -40007, below the s2 minimum); end: return
    static byte[] gotoWBackward(String cls) throws Exception {
        int HEAD = 2, GOTOW = 40009, END = 40014, len = END + 1;
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        header(d, cls, "()V");
        u2(d, 7); u4(d, len + 44); u2(d, 1); u2(d, 1);
        u4(d, len);
        u1(d, 0x04);                                    // 0: iconst_1
        u1(d, 0x3B);                                    // 1: istore_0
        for (int i = 2; i < 40002; i++) u1(d, 0x00);    // 2..40001: nop
        u1(d, 0x84); u1(d, 0); u1(d, 0xFF);             // 40002: iinc 0,-1
        u1(d, 0x1A);                                    // 40005: iload_0
        u1(d, 0x9E); u2(d, END - 40006);                // 40006: ifle -> 40014
        u1(d, 0xC8); u4(d, HEAD - GOTOW);               // 40009: goto_w -> 2
        u1(d, 0xB1);                                    // 40014: return
        u2(d, 0); u2(d, 1);
        u2(d, 8); u4(d, 26); u2(d, 3);
        frame(d, HEAD, new int[]{1}, new int[0]);
        frame(d, GOTOW - HEAD - 1, new int[]{1}, new int[0]);
        frame(d, END - GOTOW - 1, new int[]{1}, new int[0]);
        u2(d, 0);
        return o.toByteArray();
    }

    // 0: iconst_0; 1: tableswitch (operands 4-aligned), default and the single
    // case both -> 40000 (offset +39999, beyond s2); 40000: iconst_5; ireturn
    static byte[] switchBigOffset(String cls) throws Exception {
        int target = 40000, len = target + 2;
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        DataOutputStream d = new DataOutputStream(o);
        header(d, cls, "()I");
        u2(d, 7); u4(d, len + 34); u2(d, 1); u2(d, 0);
        u4(d, len);
        u1(d, 0x03);                                    // 0: iconst_0
        u1(d, 0xAA); u1(d, 0); u1(d, 0);                // 1: tableswitch + 2 pad
        u4(d, target - 1); u4(d, 0); u4(d, 0); u4(d, target - 1);
        for (int i = 20; i < target; i++) u1(d, 0x00);
        u1(d, 0x08); u1(d, 0xAC);                       // 40000: iconst_5; ireturn
        u2(d, 0); u2(d, 1);
        u2(d, 8); u4(d, 16); u2(d, 2);
        frame(d, 20, new int[0], new int[0]);
        frame(d, target - 20 - 1, new int[0], new int[0]);
        u2(d, 0);
        return o.toByteArray();
    }

    public static void main(String[] args) throws Exception {
        // goto at the exact s2 maximum forward offset lands on the final return
        load("GotoS2Max", gotoBoundary("GotoS2Max")).getMethod("m").invoke(null);
        check(true, "goto +32767 (s2 max) verifies and executes");

        // forward offset beyond s2 requires goto_w
        Object r = load("GotoWFwd", gotoWForward("GotoWFwd")).getMethod("m").invoke(null);
        check(((Integer) r).intValue() == 1, "goto_w +40000 executes to completion");

        // backward offset below s2 minimum requires goto_w; loop still terminates
        long t0 = System.nanoTime();
        load("GotoWBack", gotoWBackward("GotoWBack")).getMethod("m").invoke(null);
        check(System.nanoTime() - t0 < 5_000_000_000L, "goto_w -40007 loop terminates quickly");

        // tableswitch offsets are s4 and may exceed the s2 range
        Object s = load("SwitchBig", switchBigOffset("SwitchBig")).getMethod("m").invoke(null);
        check(((Integer) s).intValue() == 5, "tableswitch +39999 dispatches and returns");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
