# java.lang.classfile.instruction.LineNumber — 设计案

## 1. API 功能
`java.lang.classfile.instruction.LineNumber`（interface）。工厂/静态方法：of。查询/实例方法：line。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `LineNumberTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.instruction.LineNumber` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`LineNumberTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.instruction.LineNumber")` + public/interface/继承 + 关键成员。黑盒（`LineNumberTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

### 深化（发射-回读矩阵批）
- 发射-回读：lineNumber(9) → 回读 line()==9 + LineNumberTable 属性条目精确；u2 范围实测：of(65535) OK / of(65536) IAE "line number out of range of u2"；Integer.MAX_VALUE 同 IAE。
- ClassFile.DropOption DROP_LINE_NUMBERS 剥离伪指令但保留属性实测；javac 无 -g 时无 LNT（注意）。断言 9→30（实测运行计数）。
