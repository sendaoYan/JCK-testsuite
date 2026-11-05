# java.lang.classfile.Instruction — 设计案

## 1. API 功能
`java.lang.classfile.Instruction`（interface，extends `CodeElement`）。方法：`opcode()`（Opcode）、`sizeInBytes()`（int）。子类型族（instruction 包）：NopInstruction/ReturnInstruction/BranchInstruction/ConstantInstruction(LoadConstantInstruction)/FieldInstruction/InvokeInstruction/InvokeDynamicInstruction/NewPrimitiveArrayInstruction/.../TableSwitchInstruction/LookupSwitchInstruction/DiscontinuedInstruction 族。各类型多有静态 `of(...)` 工厂。

## 2. Java 用户如何使用
经 `CodeBuilder` 发射（如 `iconst_1()/bipush(7)/ldc(x)/getstatic(...)/iinc(i,v)/tableswitch(...)`）→ parse → `code.elementList()` 中 instanceof Instruction → `opcode()`/`sizeInBytes()`。**契约：定长指令 `sizeInBytes()==Opcode.sizeIfFixed()`；变长（TABLESWITCH/LOOKUPSWITCH）`sizeIfFixed()==-1` 而 `sizeInBytes()>0`**。`sum(Instruction.sizeInBytes())==codeLength` 恒成立（填充字节以 NOP 形态计入）。宽形式自动选择：`iinc(256,..)`→IINC_W(6B, isWide)、`iload(300)`→ILOAD_W(4B)。

## 3. 参考来源
- JDK 27 javadoc + `javap java.lang.classfile.Instruction` + Opcode。
- OpenJDK `test/jdk/jdk/classfile/`（InstructionTest/ShortJumpsFixTest/TestCodeBuilder）。
- 本机实测：Opcode.values()==213；IINC=3B/IINC_W=6B/ILOAD_W=4B/LDC=2B/LDC2_W=3B；javac `switch(1/5/9)`→LOOKUPSWITCH 键升序；`ldc(100000L)`→LDC2_W；iinc 于未初始化局部槽 → verify "Bad local variable type ... bogus_type"。

## 4. 测试用例设计 + 易错规范细节
- 白盒（原有）+ 黑盒（原有：NopInstruction.of/ReturnInstruction.of 的 opcode/size）。
- `testEmitReadbackMatrix`（深化）：发射 12+ 条指令族（iconst/bipush/iadd/ldc String/getstatic/nop/iinc/自动宽 iinc+iload）→ 回读逐条断言 **opcode 非 null、instanceof CodeElement、定长 sizeInBytes==sizeIfFixed**；opcode 序列 contains 断言（IINC_W/ILOAD_W 自动宽）；isWide 双断言；宽/窄固定尺寸精确表（IINC 3、IINC_W 6、ILOAD_W 4）；**sum(sizeInBytes)==codeLength==codeArray().length 不变量**；verify 干净（宽槽位先 istore 初始化再访问，否则 VerifyError bogus_type）。
- `testSwitchInstructions`（深化）：tableswitch 发射→回读（lowValue/highValue/cases/defaultTarget is Label/case target is Label）；sizeIfFixed==-1 + sizeInBytes>12；**真类 javac 产物**：switch(1/5/9)→LOOKUPSWITCH 键升序 [1,5,9]；`ldc2()`→LDC2_W；`ldcs()`→LDC；LDC/LDC2_W 固定宽 2/3。
- `testFactoriesAndRun`（深化）：ReturnInstruction.of(TypeKind) 五态（I/A/V/L RETURN 族）；**分类互斥**（Instruction NOT PseudoInstruction 双向运行时断言）；Opcode.values()==213；DynInstruction+seq imul+iadd → defineClass → go(7,0)==42 运行闭环。
- 易错点：**iinc/iload 到未初始化槽 build 过栈检但 verify 失败**（须先 istore）；`tableswitch(low, high, default, cases)` 的 low/high 是 int；填充字节在回读中呈现为 NOP 指令。
