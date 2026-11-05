# java.lang.classfile.CodeElement — 设计案

## 1. API 功能
`java.lang.classfile.CodeElement`（空标记接口，extends `ClassFileElement`）。两大实现族：`Instruction`（全部字节码指令）与 `PseudoInstruction`（LabelTarget/LineNumber/LocalVariable/LocalVariableType/ExceptionCatch/CharacterRange）；另有 `StackMapTableAttribute`（**唯一实现 CodeElement 的属性**，实测）与 `CustomAttribute`。**非**实现者：SourceFile/Deprecated/ConstantValue/Exceptions 属性、ClassFileVersion/Superclass/Interfaces、FieldModel/MethodModel/ClassModel。

## 2. Java 用户如何使用
`CodeModel.elementList()` 的元素类型（Instruction + PseudoInstruction **二分恰好划分全体**，实测：`instr + pseudo == elementList.size()`）；`CodeTransform.accept(CodeBuilder, CodeElement)` 的元素币种；`cb.with(CodeElement)` 逐条转发。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.CodeElement`、`attribute.StackMapTableAttribute`。
- OpenJDK `test/jdk/jdk/classfile/`（CodePipelinesTest 等）。
- 本机实测：`LocalVariable.of(0,"x",CD_int,null,null)` → **NPE**（labels 必须非空）；StackMapTable 在 attributes() 而不在 elementList。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：NopInstruction/LineNumber 两正例）。
- `testPositiveFamily`（深化）：工厂（Nop/ReturnInstruction/LineNumber/CustomAttribute）+ 发射产物（try-catch+LV+LN build）——**Instruction+PseudoInstruction 二分划分**（计数和==elementList.size()）、逐伪指令族计数（lt/ec/lv/ln）；**StackMapTableAttribute is CodeElement**；真类全方法 code 元素循环 instanceof。
- `testNegativeFamily`（深化）：SourceFile/Deprecated/ConstantValue/Exceptions/ClassFileVersion/Interfaces NOT；FieldModel/MethodModel/ClassModel NOT；三个 isAssignableFrom 正向关系（Instruction/PseudoInstruction ← CodeElement ← ClassFileElement）。
- `testTransformChannel`（深化）：`LocalVariable.of(null labels)` **NPE 负例**；DynCodeElement+seq → CodeElement 谓词剥 NOP → 指令序列 [ICONST_2, IRETURN] → defineClass → go()==2 运行闭环 + verify。
- 易错点：伪指令工厂（LocalVariable.of 等）**labels 不可为 null**（NPE）；StackMapTable 是属性却在 CodeElement 族里（编译器认可），但不出现在 elementList。
