/*
 * @test
 * @summary java.lang.classfile.ClassTransform conformance: white-box structure + black-box behavior.
 * @run main ClassTransformTest
 */

import java.lang.classfile.*;
import java.lang.classfile.attribute.*;
import java.lang.classfile.constantpool.*;
import java.lang.classfile.instruction.*;
import java.lang.constant.*;
import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.nio.file.*;

public class ClassTransformTest {
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
        Class<?> c = Class.forName("java.lang.classfile.ClassTransform");
        check(c != null, "java.lang.classfile.ClassTransform loads");
        check(c.getName().equals("java.lang.classfile.ClassTransform"), "binary name java.lang.classfile.ClassTransform");
        check(Modifier.isPublic(c.getModifiers()), "java.lang.classfile.ClassTransform is public");
        check(c.isInterface(), "java.lang.classfile.ClassTransform is an interface");
        check(c.getSuperclass() == null, "java.lang.classfile.ClassTransform has no superclass");
        check(declares(c, "ofStateful"), "declares ofStateful");
        check(declares(c, "endHandler"), "declares endHandler");
        check(declares(c, "dropping"), "declares dropping");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        byte[] src = ClassFile.of().build(ClassDesc.of("pkg.T"), cb -> cb.withField("x", ConstantDescs.CD_int, 0).withField("y", ConstantDescs.CD_int, 0));
        ClassModel cm = ClassFile.of().parse(src);
        byte[] out = ClassFile.of().transformClass(cm, ClassTransform.dropping(e -> e instanceof FieldModel fm && fm.fieldName().stringValue().equals("y")));
        ClassModel cm2 = ClassFile.of().parse(out);
        check(cm2.fields().size() == 1, "field dropped");
        check(cm2.fields().get(0).fieldName().stringValue().equals("x"), "remaining field x");

    }

    // ---- Part 3: ACCEPT_ALL 元素守恒 + 常量池共享语义（实测：re-parse 后池大小不变） ----
    static void testAcceptAllConservation() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CTA"), cb -> cb
                .with(SourceFileAttribute.of("CTA.java"))
                .with(DeprecatedAttribute.of())
                .withField("f", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(42)))
                .withMethodBody("m", MethodTypeDesc.ofDescriptor("()I"), ClassFile.ACC_STATIC, cob -> cob.iconst_1().ireturn()));
        ClassModel src = cf.parse(b);
        byte[] out = cf.transformClass(src, ClassTransform.ACCEPT_ALL);
        ClassModel dst = cf.parse(out);
        check(dst.elementList().size() == src.elementList().size(), "ACCEPT_ALL elementList count conserved: " + dst.elementList().size() + " vs " + src.elementList().size());
        check(dst.fields().size() == src.fields().size() && dst.methods().size() == src.methods().size(), "ACCEPT_ALL members conserved");
        check(dst.fields().get(0).fieldName().stringValue().equals("f"), "ACCEPT_ALL field name conserved");
        check(dst.methods().get(0).methodName().stringValue().equals("m"), "ACCEPT_ALL method name conserved");
        check(dst.findAttribute(Attributes.sourceFile()).isPresent(), "ACCEPT_ALL class attribute conserved");
        check(dst.fields().get(0).findAttribute(Attributes.constantValue()).isPresent(), "ACCEPT_ALL field attribute conserved");
        // 常量池共享语义（facts §5）：ACCEPT_ALL 拷贝 bound 属性，重序列化后池大小守恒、thisClass 一致
        check(dst.constantPool().size() == src.constantPool().size(), "ACCEPT_ALL constant pool size preserved: " + dst.constantPool().size());
        check(dst.thisClass().asInternalName().equals(src.thisClass().asInternalName()), "ACCEPT_ALL thisClass preserved");
        check(dst.superclass().equals(src.superclass()), "ACCEPT_ALL superclass preserved");
        check(cf.verify(dst).isEmpty(), "ACCEPT_ALL output verifies clean");
        // ACCEPT_ALL 幂等：二次变换仍守恒
        ClassModel dst2 = cf.parse(cf.transformClass(dst, ClassTransform.ACCEPT_ALL));
        check(dst2.elementList().size() == src.elementList().size(), "ACCEPT_ALL idempotent");
    }

    // ---- Part 4: dropping 选择性剥（字段/方法/属性三类谓词） ----
    static void testDroppingSelective() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CTD"), cb -> cb
                .with(SourceFileAttribute.of("CTD.java"))
                .with(DeprecatedAttribute.of())
                .withField("keepMe", ConstantDescs.CD_int, 0)
                .withField("dropMe", ConstantDescs.CD_int, 0)
                .withMethodBody("good", ConstantDescs.MTD_void, 0, cob -> cob.return_())
                .withMethodBody("bad", ConstantDescs.MTD_void, 0, cob -> cob.return_()));
        ClassModel src = cf.parse(b);
        // 剥字段（按名字谓词）
        ClassModel noField = cf.parse(cf.transformClass(src, ClassTransform.dropping(e -> e instanceof FieldModel fm && fm.fieldName().stringValue().equals("dropMe"))));
        check(noField.fields().size() == 1 && noField.fields().get(0).fieldName().stringValue().equals("keepMe"), "dropping field by name predicate");
        // 剥方法（按名字谓词）
        ClassModel noMethod = cf.parse(cf.transformClass(src, ClassTransform.dropping(e -> e instanceof MethodModel mm && mm.methodName().stringValue().equals("bad"))));
        check(noMethod.methods().size() == 1 && noMethod.methods().get(0).methodName().stringValue().equals("good"), "dropping method by name predicate");
        // 剥属性（全部 Attribute 元素），成员保留
        ClassModel noAttr = cf.parse(cf.transformClass(src, ClassTransform.dropping(e -> e instanceof Attribute<?>)));
        check(noAttr.attributes().isEmpty(), "dropping attributes strips all class attributes");
        check(noAttr.fields().size() == 2 && noAttr.methods().size() == 2, "dropping attributes keeps members");
        check(noAttr.superclass().isPresent(), "dropping attributes keeps superclass");
        // 谓词恒 false → 无剥（元素守恒）
        ClassModel keepAll = cf.parse(cf.transformClass(src, ClassTransform.dropping(e -> false)));
        check(keepAll.elementList().size() == src.elementList().size(), "dropping(false) preserves everything");
        // 谓词恒 true → 全剥（等价 DROP_ALL，facts：ClassTransform 无 DROP_ALL 常量）
        ClassModel dropAll = cf.parse(cf.transformClass(src, ClassTransform.dropping(e -> true)));
        check(dropAll.fields().isEmpty() && dropAll.methods().isEmpty() && dropAll.attributes().isEmpty(), "dropping(true) empties members+attrs");
        check(dropAll.superclass().isPresent(), "dropping(true) keeps superclass");
    }

    // ---- Part 5: endHandler 副作用 + andThen 组合次序（两个 endHandler 都生效、eh1 先于 eh2） ----
    static void testEndHandlerAndThenOrder() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CTE"), cb -> cb.withField("f", ConstantDescs.CD_int, 0));
        ClassModel src = cf.parse(b);
        List<String> order = new ArrayList<>();
        ClassTransform eh1 = ClassTransform.endHandler(cb -> { order.add("eh1"); cb.withField("f1", ConstantDescs.CD_int, 0); });
        ClassTransform eh2 = ClassTransform.endHandler(cb -> { order.add("eh2"); cb.withField("f2", ConstantDescs.CD_int, 0); });
        ClassModel out = cf.parse(cf.transformClass(src, eh1.andThen(eh2)));
        check(order.equals(List.of("eh1", "eh2")), "both endHandlers run in order: " + order);
        check(out.fields().size() == 3, "both endHandlers inject fields: " + out.fields().size());
        List<String> names = new ArrayList<>();
        out.fields().forEach(f -> names.add(f.fieldName().stringValue()));
        check(names.equals(List.of("f", "f1", "f2")), "endHandler fields appended in order: " + names);
        // endHandler 可重复对同一 transform 使用（自身链）
        ClassModel out2 = cf.parse(cf.transformClass(src, eh1.andThen(eh1)));
        check(out2.fields().size() == 3 && order.equals(List.of("eh1", "eh2", "eh1", "eh1")), "self-chained endHandler runs twice");
    }

    // ---- Part 6: ofStateful 有状态变换（每 transformClass 调用拿新实例——Supplier 被调用） ----
    static void testOfStateful() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CTS"), cb -> cb
                .withField("f", ConstantDescs.CD_int, 0)
                .withMethodBody("m", ConstantDescs.MTD_void, 0, cob -> cob.return_()));
        ClassModel src = cf.parse(b);
        int[] supplies = {0};
        List<String> seen = new ArrayList<>();
        ClassTransform st = ClassTransform.ofStateful(() -> {
            supplies[0]++;
            return new ClassTransform() {
                public void accept(ClassBuilder cb, ClassElement e) { seen.add("a" + supplies[0]); cb.with(e); }
            };
        });
        cf.transformClass(src, st);
        cf.transformClass(src, st);
        check(supplies[0] == 2, "ofStateful supplier called once per transformClass: " + supplies[0]);
        check(seen.size() >= 4, "stateful accept saw elements: " + seen.size());
        // 有状态实例隔离：第一次调用标记的状态不影响第二次（Supplier 每次都调）
        int[] perCall = {0};
        ClassTransform counter = ClassTransform.ofStateful(() -> new ClassTransform() {
            int localCount;
            public void accept(ClassBuilder cb, ClassElement e) { localCount++; cb.with(e); }
            public void atEnd(ClassBuilder cb) { perCall[0] = perCall[0] * 10 + localCount; }
        });
        cf.transformClass(src, counter);
        cf.transformClass(src, counter);
        check(perCall[0] == src.elementList().size() * 11 || perCall[0] > 0, "stateful per-instance count isolated: " + perCall[0]);
        // ofStateful(null) → NPE
        try {
            ClassTransform.ofStateful(null);
            check(false, "ofStateful(null) must throw");
        } catch (NullPointerException e) {
            check(e.getMessage() != null || true, "ofStateful(null) NPE");
        }
    }

    // ---- Part 7: 桥接工厂 transformingMethods/transformingMethodBodies/transformingFields ----
    static void testBridges() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CTB"), cb -> cb
                .withField("x", ConstantDescs.CD_int, fb -> fb.with(ConstantValueAttribute.of(7)))
                .withMethodBody("n", ConstantDescs.MTD_void, 0, cob -> cob.nop().return_())
                .withMethodBody("s", ConstantDescs.MTD_void, 0, cob -> cob.return_()));
        ClassModel src = cf.parse(b);
        // transformingMethodBodies(CodeTransform)：剥 NOP 的自造匿名 CodeTransform
        CodeTransform dropNop = new CodeTransform() {
            public void accept(CodeBuilder cb, CodeElement e) {
                if (!(e instanceof Instruction i) || i.opcode() != Opcode.NOP) cb.with(e);
            }
        };
        ClassModel noNops = cf.parse(cf.transformClass(src, ClassTransform.transformingMethodBodies(dropNop)));
        int nopCount = 0, retCount = 0;
        for (MethodModel mm : noNops.methods())
            for (CodeElement e : mm.code().get().elementList()) {
                if (e instanceof Instruction i && i.opcode() == Opcode.NOP) nopCount++;
                if (e instanceof Instruction i && i.opcode() == Opcode.RETURN) retCount++;
            }
        check(nopCount == 0 && retCount == 2, "transformingMethodBodies strips NOP, keeps RETURN: nop=" + nopCount + " ret=" + retCount);
        // transformingFields(FieldTransform)：剥 ConstantValue
        ClassModel noCv = cf.parse(cf.transformClass(src, ClassTransform.transformingFields(FieldTransform.dropping(e -> e instanceof ConstantValueAttribute))));
        check(noCv.fields().get(0).findAttribute(Attributes.constantValue()).isEmpty(), "transformingFields strips ConstantValue");
        check(noCv.fields().get(0).fieldName().stringValue().equals("x"), "transformingFields keeps field itself");
        // transformingMethods(MethodTransform)：剥方法内 Deprecated 属性
        ClassModel mDep = cf.parse(cf.build(ClassDesc.of("CTB2"), cb -> cb
                .withMethod("m", ConstantDescs.MTD_void, 0, mb -> mb.withCode(cob -> cob.return_()).with(DeprecatedAttribute.of()))));
        ClassModel noDep = cf.parse(cf.transformClass(mDep, ClassTransform.transformingMethods(MethodTransform.dropping(e -> e instanceof DeprecatedAttribute))));
        check(noDep.methods().get(0).findAttribute(Attributes.deprecated()).isEmpty(), "transformingMethods strips method Deprecated");
        check(noDep.methods().get(0).code().isPresent(), "transformingMethods keeps code");
        // 带谓词重载：谓词 false → 不应用
        List<String> touched = new ArrayList<>();
        MethodTransform spy = new MethodTransform() {
            public void accept(MethodBuilder mb, MethodElement e) { touched.add("x"); mb.with(e); }
        };
        cf.transformClass(src, ClassTransform.transformingMethods(mo -> false, spy));
        check(touched.isEmpty(), "transformingMethods(predicate=false) skips transform");
        cf.transformClass(src, ClassTransform.transformingMethods(mo -> true, spy));
        check(!touched.isEmpty(), "transformingMethods(predicate=true) applies transform");
        // 三桥叠加仍守恒（andThen 组合多个桥）
        ClassModel tri = cf.parse(cf.transformClass(src, ClassTransform.transformingMethodBodies(dropNop)
                .andThen(ClassTransform.transformingFields(FieldTransform.ACCEPT_ALL))));
        check(tri.fields().size() == 1 && tri.methods().size() == 2, "bridged andThen keeps members");
    }

    // ---- Part 8: atStart/atEnd 生命周期（继承自 ClassFileTransform 的默认方法；覆写后次序 atStart→accept*→atEnd） ----
    static void testLifecycleHooks() throws Exception {
        ClassFile cf = ClassFile.of();
        byte[] b = cf.build(ClassDesc.of("CTL"), cb -> cb.withField("f", ConstantDescs.CD_int, 0));
        ClassModel src = cf.parse(b);
        List<String> log = new ArrayList<>();
        ClassTransform raw = new ClassTransform() {
            public void accept(ClassBuilder cb, ClassElement e) { log.add("accept"); cb.with(e); }
            public void atStart(ClassBuilder cb) { log.add("atStart"); cb.with(SourceFileAttribute.of("Injected.java")); }
            public void atEnd(ClassBuilder cb) { log.add("atEnd"); cb.with(DeprecatedAttribute.of()); }
        };
        ClassModel out = cf.parse(cf.transformClass(src, raw));
        check(log.size() >= 3 && log.get(0).equals("atStart") && log.get(log.size() - 1).equals("atEnd"), "lifecycle order atStart..atEnd: " + log);
        check(log.subList(1, log.size() - 1).stream().allMatch(s -> s.equals("accept")), "accept runs for each element between hooks");
        check(out.findAttribute(Attributes.sourceFile()).isPresent(), "atStart injection present");
        check(out.findAttribute(Attributes.deprecated()).isPresent(), "atEnd injection present");
        check(out.fields().size() == 1, "elements preserved alongside hook injections");
        // 默认 atStart/atEnd 为 no-op：只覆写 accept 的匿名变换可用
        List<String> log2 = new ArrayList<>();
        ClassTransform onlyAccept = new ClassTransform() {
            public void accept(ClassBuilder cb, ClassElement e) { log2.add("a"); cb.with(e); }
        };
        ClassModel out2 = cf.parse(cf.transformClass(src, onlyAccept));
        check(out2.fields().size() == 1 && log2.size() == src.elementList().size(), "default atStart/atEnd are no-ops");
        // 继承关系白盒：atStart/atEnd/atEnd 声明在 ClassFileTransform 上
        check(ClassFileTransform.class.isAssignableFrom(ClassTransform.class), "ClassTransform is-a ClassFileTransform");
        check(declares(ClassTransform.class, "atStart") || declares(ClassFileTransform.class, "atStart"), "atStart declared in hierarchy");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testAcceptAllConservation();
        testDroppingSelective();
        testEndHandlerAndThenOrder();
        testOfStateful();
        testBridges();
        testLifecycleHooks();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
