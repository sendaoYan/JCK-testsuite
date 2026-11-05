/*
 * @test
 * @summary java.lang.classfile.attribute.ModuleHashInfo conformance: white-box structure + black-box behavior.
 * @run main ModuleHashInfoTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;

public class ModuleHashInfoTest {
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
    static final ClassReader[] cap = new ClassReader[1];
    static final BufWriter[] cap2 = new BufWriter[1];
    static final BranchInstruction[] br = new BranchInstruction[1];
    static final SwitchCase[] sc = new SwitchCase[1];
    static final TableSwitchInstruction[] ts = new TableSwitchInstruction[1];
    static final LookupSwitchInstruction[] ls = new LookupSwitchInstruction[1];
    static final ExceptionCatch[] ec = new ExceptionCatch[1];
    static final TypeAnnotation.TypeArgumentTarget[] ta = new TypeAnnotation.TypeArgumentTarget[1];
    static final TypeAnnotation.LocalVarTarget[] lvt = new TypeAnnotation.LocalVarTarget[1];
    static final TypeAnnotation.LocalVarTargetInfo[] lvi = new TypeAnnotation.LocalVarTargetInfo[1];
    static final StackMapFrameInfo.UninitializedVerificationTypeInfo[] uv = new StackMapFrameInfo.UninitializedVerificationTypeInfo[1];
    static final TypeAnnotation.OffsetTarget[] ot = new TypeAnnotation.OffsetTarget[1];

    // ---- custom attribute support for black-box above ----
    static class CA extends CustomAttribute<CA> { CA(AttributeMapper<CA> m) { super(m); } }
    static class CA2 extends CustomAttribute<CA2> { CA2(AttributeMapper<CA2> m) { super(m); } }
    static class CA3 extends CustomAttribute<CA3> { CA3(AttributeMapper<CA3> m) { super(m); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.classfile.attribute.ModuleHashInfo");
        check(c != null, "java.lang.classfile.attribute.ModuleHashInfo loads");
        check(c.getName().equals("java.lang.classfile.attribute.ModuleHashInfo"), "binary name java.lang.classfile.attribute.ModuleHashInfo");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.attribute.ModuleHashInfo is public");
        check(c.isInterface(), "java.lang.classfile.attribute.ModuleHashInfo is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.attribute.ModuleHashInfo has no superclass");
        check(declares(c, "moduleName"), "declares moduleName");
        check(declares(c, "hash"), "declares hash");
        check(declares(c, "of"), "declares of");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        ModuleHashInfo h = ModuleHashInfo.of(ModuleDesc.of("java.base"), new byte[]{1,2,3});
        check(h.moduleName().asSymbol().equals(ModuleDesc.of("java.base")), "moduleName");
        check(h.hash().length == 3 && h.hash()[2] == 3, "hash");

    }

    // ---- Part 3: 双工厂全重载 + 访问器矩阵 ----
    static void testFactories() {
        ConstantPoolBuilder cp = ConstantPoolBuilder.of();
        ModuleEntry me = cp.moduleEntry(ModuleDesc.of("m.a"));
        // 条目版
        ModuleHashInfo e = ModuleHashInfo.of(me, new byte[]{1, 2, 3});
        check(e.moduleName() == me, "entry factory identity");
        check(e.moduleName().asSymbol().name().equals("m.a"), "entry factory name");
        check(e.moduleName().matches(ModuleDesc.of("m.a")), "matches()");
        check(e.hash().length == 3, "entry factory hash len");
        // 符号版
        ModuleHashInfo s = ModuleHashInfo.of(ModuleDesc.of("m.b"), new byte[0]);
        check(s.moduleName().asSymbol().name().equals("m.b"), "symbol factory name");
        check(s.hash().length == 0, "empty hash legal");
        // hash 内容精确
        byte[] h = {10, 20, 30, 40};
        ModuleHashInfo t = ModuleHashInfo.of(ModuleDesc.of("m.c"), h);
        check(t.hash()[0] == 10 && t.hash()[1] == 20 && t.hash()[2] == 30 && t.hash()[3] == 40, "hash bytes exact");
        // 注：hash() 不做防御拷贝（返回内部数组）——JDK 实测语义，断言共享
        byte[] mut = {1};
        ModuleHashInfo m = ModuleHashInfo.of(ModuleDesc.of("m.d"), mut);
        mut[0] = 99;
        check(m.hash()[0] == 99, "hash array shared (no defensive copy)");
    }

    // ---- Part 4: null 负例 ----
    static void testNegatives() {
        try { ModuleHashInfo.of((ModuleDesc) null, new byte[1]); check(false, "of((ModuleDesc)null,..) must NPE"); }
        catch (NullPointerException e) {
            check(e.getMessage() != null && e.getMessage().contains("moduleDesc"), "moduleDesc null NPE msg");
        }
        try { ModuleHashInfo.of(ModuleDesc.of("m"), null); check(false, "of(..,null) must NPE"); }
        catch (NullPointerException e) { check(true, "hash null NPE"); }
        try { ModuleHashInfo.of((ModuleEntry) null, new byte[1]); check(false, "of((ModuleEntry)null,..) must NPE"); }
        catch (NullPointerException e) { check(true, "entry null NPE"); }
    }

    // ---- Part 5: 经 ModuleHashes → buildModule → re-parse 字节级回读 ----
    static void testRoundtrip() {
        ClassFile cf = ClassFile.of();
        byte[] hash = new byte[32];
        for (int i = 0; i < 32; i++) hash[i] = (byte) i;
        ModuleAttribute ma = ModuleAttribute.of(ModuleDesc.of("m.r"), mb -> {});
        byte[] b = cf.buildModule(ma, cb -> cb
                .with(ModuleHashesAttribute.of("SHA-256", ModuleHashInfo.of(ModuleDesc.of("dep"), hash))));
        ModuleHashInfo r = cf.parse(b).findAttribute(Attributes.moduleHashes()).get().hashes().get(0);
        check(r.moduleName().asSymbol().name().equals("dep"), "roundtrip module name");
        check(r.hash().length == 32, "roundtrip hash length");
        boolean all = true;
        for (int i = 0; i < 32; i++) all &= (r.hash()[i] == (byte) i);
        check(all, "roundtrip 32 hash bytes exact");
        check(cf.verify(b).isEmpty(), "verifies");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testFactories();
        testNegatives();
        testRoundtrip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
