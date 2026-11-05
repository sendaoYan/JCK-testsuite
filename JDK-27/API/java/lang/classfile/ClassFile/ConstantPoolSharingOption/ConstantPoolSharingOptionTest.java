/*
 * @test
 * @summary java.lang.classfile.ClassFile.ConstantPoolSharingOption conformance: white-box structure + black-box behavior.
 * @run main ConstantPoolSharingOptionTest
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

public class ConstantPoolSharingOptionTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassFile$ConstantPoolSharingOption");
        check(c != null, "java.lang.classfile.ClassFile.ConstantPoolSharingOption loads");
        check(c.getName().equals("java.lang.classfile.ClassFile$ConstantPoolSharingOption"), "binary name java.lang.classfile.ClassFile$ConstantPoolSharingOption");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassFile.ConstantPoolSharingOption is public");
        check(c.isEnum(), "java.lang.classfile.ClassFile.ConstantPoolSharingOption is an enum");
        check(c.getSuperclass() == Enum.class, "java.lang.classfile.ClassFile.ConstantPoolSharingOption superclass is java.lang.Enum");
        Object[] ec = c.getEnumConstants();
        check(ec.length == 2, "2 enum constants");
        java.util.Set<String> es = new java.util.HashSet<>();
        for (Object e : ec) es.add(((Enum<?>) e).name());
        check(es.equals(java.util.Set.of("SHARED_POOL", "NEW_POOL")), "exact enum constant names");
        check(declares(c, "values"), "declares values");
        check(declares(c, "valueOf"), "declares valueOf");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        check(ClassFile.ConstantPoolSharingOption.SHARED_POOL != ClassFile.ConstantPoolSharingOption.NEW_POOL, "two constants");
        check(ClassFile.ConstantPoolSharingOption.values().length == 2, "2 constants");
        ClassFile cf = ClassFile.of(ClassFile.ConstantPoolSharingOption.NEW_POOL);
        check(cf != null, "usable as option");

    }

    // ---- Part 3: option semantics measured on JDK 27 ----
    static void testOptionSemantics() throws Exception {
        ClassFile cf = ClassFile.of();

        // enum matrix
        check(ClassFile.ConstantPoolSharingOption.values()[0] == ClassFile.ConstantPoolSharingOption.SHARED_POOL,
                "ordinal 0 is SHARED_POOL (the default semantic)");
        for (ClassFile.ConstantPoolSharingOption o : ClassFile.ConstantPoolSharingOption.values())
            check(ClassFile.ConstantPoolSharingOption.valueOf(o.name()) == o, "valueOf round-trip " + o);
        check(ClassFile.ConstantPoolSharingOption.SHARED_POOL.compareTo(ClassFile.ConstantPoolSharingOption.NEW_POOL) < 0,
                "SHARED_POOL < NEW_POOL by declaration order");

        // source class with one field and one method
        byte[] src = cf.build(ClassDesc.of("CpsT1"), cb -> cb
                .withField("f1", ConstantDescs.CD_int, ClassFile.ACC_PRIVATE)
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC,
                        c -> c.bipush(7).ireturn()));
        ClassModel srcM = cf.parse(src);
        int srcCp = srcM.constantPool().size();

        // neutral transform: both options produce equivalent classes; pool sizes identical here
        for (ClassFile.ConstantPoolSharingOption o : ClassFile.ConstantPoolSharingOption.values()) {
            byte[] out = cf.withOptions(o).transformClass(srcM, ClassTransform.ACCEPT_ALL);
            ClassModel om = cf.parse(out);
            check(om.elementList().size() == srcM.elementList().size(),
                    o + " ACCEPT_ALL preserves element count");
            check(om.fields().size() == 1 && om.methods().size() == 1, o + " ACCEPT_ALL preserves members");
            check(om.constantPool().size() == srcCp, o + " keeps pool size on neutral transform");
        }

        // observable difference: after dropping a member, SHARED_POOL retains the (now unused) entries,
        // NEW_POOL rebuilds the pool from scratch and drops them
        ClassTransform dropFields = ClassTransform.dropping(e -> e instanceof FieldModel);
        ClassModel sharedOut = cf.parse(cf.withOptions(ClassFile.ConstantPoolSharingOption.SHARED_POOL)
                .transformClass(srcM, dropFields));
        ClassModel newOut = cf.parse(cf.withOptions(ClassFile.ConstantPoolSharingOption.NEW_POOL)
                .transformClass(srcM, dropFields));
        check(sharedOut.fields().isEmpty() && newOut.fields().isEmpty(), "both options drop the field");
        check(sharedOut.constantPool().size() == srcCp,
                "SHARED_POOL retains unused entries: " + sharedOut.constantPool().size() + " vs " + srcCp);
        check(newOut.constantPool().size() < sharedOut.constantPool().size(),
                "NEW_POOL drops unused entries: " + newOut.constantPool().size() + " < " + sharedOut.constantPool().size());

        // both products stay faithful: same this class, same method body semantics
        for (ClassModel m : java.util.List.of(sharedOut, newOut)) {
            check(m.thisClass().asInternalName().startsWith("CpsT1"), "thisClass preserved");
            boolean found = false;
            for (MethodModel mm : m.methods())
                if (mm.methodName().stringValue().equals("m")) found = mm.code().isPresent();
            check(found, "method m with code preserved");
        }

        // build side: fresh builds unaffected by the option (no shared source pool exists)
        byte[] b1 = cf.withOptions(ClassFile.ConstantPoolSharingOption.NEW_POOL)
                .build(ClassDesc.of("CpsT2"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void,
                        ClassFile.ACC_STATIC, CodeBuilder::return_));
        byte[] b2 = cf.withOptions(ClassFile.ConstantPoolSharingOption.SHARED_POOL)
                .build(ClassDesc.of("CpsT3"), cb -> cb.withMethodBody("m", ConstantDescs.MTD_void,
                        ClassFile.ACC_STATIC, CodeBuilder::return_));
        check(cf.parse(b1).methods().size() == 1 && cf.parse(b2).methods().size() == 1,
                "fresh builds succeed under both options");
        check(cf.verify(b1).isEmpty() && cf.verify(b2).isEmpty(), "fresh builds verify clean under both options");
        check(cf.withOptions(ClassFile.ConstantPoolSharingOption.NEW_POOL) != cf, "withOptions returns new instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testOptionSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
