# java.lang.classfile.attribute.LocalVariableTypeInfo — 设计案

## 1. API 功能
`java.lang.classfile.attribute.LocalVariableTypeInfo`（interface）。工厂/静态方法：(none)。查询/实例方法：startPc, length, name, signature, slot。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `LocalVariableTypeInfoTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.attribute.LocalVariableTypeInfo` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`LocalVariableTypeInfoTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.attribute.LocalVariableTypeInfo")` + public/interface/继承 + 关键成员。黑盒（`LocalVariableTypeInfoTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化）
新增维度：无公开工厂——cob.localVariableType 双重载（Signature.parseFrom / Utf8Entry 通道）→ 发射→re-parse slot/name/signature（泛型签名精确串）/startPc/length + LocalVariableType 伪指令计数 + DROP_DEBUG 双侧交叉 + javac 默认不产 LVTT。
易错点（实测）：length 覆盖整个方法体（含 return 指令，aconst_null+pop+return_ 体为 3）。
