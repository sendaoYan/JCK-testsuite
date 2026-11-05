# java.lang.classfile.instruction.BranchInstruction — 设计案

## 1. API 功能
`java.lang.classfile.instruction.BranchInstruction`（interface）。工厂/静态方法：of。查询/实例方法：target。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `BranchInstructionTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.instruction.BranchInstruction` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`BranchInstructionTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.instruction.BranchInstruction")` + public/interface/继承 + 关键成员。黑盒（`BranchInstructionTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（发射-回读矩阵批）
- testEmitReadback：if_icmpge 发射回读 sizeInBytes(3)、kind==BRANCH、labelToBci(target)==7 精确位置；goto/goto_w size 3/5；一元条件+引用条件族（ifeq/ifne/ifnull/ifnonnull/if_acmpeq/if_acmpne）顺序表；int 比较全 6 族（if_icmplt/ge/gt/le/eq/ne）顺序表；同目标多分支 target== 同一 Label 实例。
- 工厂：of(GOTO,l) target 身份（==）保持、size 3；of(null,l)/of(GOTO,null) NPE、of(NOP,l)/of(IADD,l) IAE。断言 10→32（实测运行计数）。
