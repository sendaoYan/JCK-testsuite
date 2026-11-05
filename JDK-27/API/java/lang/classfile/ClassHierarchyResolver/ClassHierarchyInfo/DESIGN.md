# java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo — 设计案

## 1. API 功能
`java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo`（interface）。工厂/静态方法：ofClass, ofInterface。查询/实例方法：(none)。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ClassHierarchyInfoTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassHierarchyResolver.ClassHierarchyInfo` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ClassHierarchyInfoTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassHierarchyResolver$ClassHierarchyInfo")` + public/interface/继承 + 关键成员。黑盒（`ClassHierarchyInfoTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化，9 → 37 断言）
- 白盒追加：**JDK-27 形态为 sealed 接口**（permits 内部 impl），公开面仅 ofClass/ofInterface 两个静态工厂、**无任何实例访问器**（declared methods 全 static； superclass()/interfaces() 不存在）。
- `testValueSemantics`：同参两次构造不同实例但 **equals/hashCode 值相等**；ofClass vs ofInterface 不等；不同父类不等；equals(null) false；toString 实测格式 `ClassHierarchyInfoImpl[superClass=..., isInterface=...]`（ofClass 含父名+isInterface=false，ofInterface 含 Object+isInterface=true）；**ofClass(null) 不抛 NPE**（superClass=null 占位，实测锁定）。
- `testResolverIntegration`：resolver 体系回读——of(map) 的 info 与 ofClass(Number) 值相等；defaultResolver 的 String info 含 Object；**cached(Supplier) 透传保持实例同一性（==）**；未知名 → null。
