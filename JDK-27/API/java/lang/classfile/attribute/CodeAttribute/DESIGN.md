# java.lang.classfile.attribute.CodeAttribute — 设计案

## 1. API 功能
`java.lang.classfile.attribute.CodeAttribute`（interface）。工厂/静态方法：(none)。查询/实例方法：maxLocals, maxStack, codeLength, codeArray, labelToBci。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `CodeAttributeTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.CodeAttribute` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`CodeAttributeTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.CodeAttribute")` + public/interface/继承 + 关键成员。黑盒（`CodeAttributeTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化）
新增维度：build 分支方法 → maxStack/maxLocals/codeLength/codeArray().length 全等 + codeArray 字节内容（首字节/末字节 opcode）+ labelToBci（从解析出的 LabelTarget 取 Label）+ method.findAttribute(Attributes.code()) 与 method.code() 同一实例 + try-catch exceptionHandlers（catchType Optional 精确、handler bci）+ 抽象方法无 Code + 元数据（CP_REFS/allowMultiple false）。
易错点（实测）：codeArray()[i] 是有符号 byte，比较 opcode 要 & 0xff；CodeAttribute 经 CodeModel 通道访问（method.code() cast），findAttribute(code()) 返回同实例。
