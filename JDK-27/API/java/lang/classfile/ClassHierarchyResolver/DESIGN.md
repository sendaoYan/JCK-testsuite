# java.lang.classfile.ClassHierarchyResolver — 设计案

## 1. API 功能
`java.lang.classfile.ClassHierarchyResolver`（interface）。工厂/静态方法：defaultResolver, ofResourceParsing, of, ofClassLoading。查询/实例方法：getClassInfo, orElse, cached。

## 2. Java 用户如何使用
通过 `ClassFile.of()` 构建/解析 class（或 `ConstantPoolBuilder.of()` 建常量池条目、`ClassFile.of().build()`/`parse()` 往返）得到真实实例；实例通过 `of(...)` 工厂创建后逐方法断言访问器；涉及 label 的类型在 `ClassFile.of().build(...)` 回调内用 `CodeBuilder.newLabel()` 捕获。 本测试在 `ClassHierarchyResolverTest.testBlackBox()` 中按「构建 → 解析/创建 → 组合断言」链路覆盖。

## 3. 开源代码实际用法调研
JDK 自带 `test/jdk/jdk/classfile/*.java`（如 ClassBuildingTest、SignaturesTest、ModuleBuilderTest、Utf8EntryTest、AttributesTest）与 JDK 源码自用（`src/java.base/.../java/lang/Module.java`、`jdk.jartool/.../FingerPrint.java`、`java.lang.reflect.ProxyGenerator`）大量使用 `java.lang.classfile` 族 API；本测试的 `java.lang.classfile.ClassHierarchyResolver` 断言按上述真实用法模式编写。

## 4. 测试用例设计 + 自动化实现
白盒（`ClassHierarchyResolverTest.testWhiteBox()`）：`Class.forName("java.lang.classfile.ClassHierarchyResolver")` + public/interface/继承 + 关键成员。黑盒（`ClassHierarchyResolverTest.testBlackBox()`）：多步组合操作——构造/解析真实实例 → 逐访问器精确断言 → 常量/标签/往返校验。

## 5. 深化条目（2026-11-05 契约深化，11 → 64 断言）
- **JDK-27 契约差异（反射+javap 锁定）：`getClassInfo(ClassDesc)` 返回裸 `ClassHierarchyInfo`（可空），非 Optional**；接口 @FunctionalInterface（lambda 可定制）；全部静态工厂 of(Collection,Map)/ofClassLoading(CL/Lookup)/ofResourceParsing(CL/Function)/defaultResolver 与默认方法 orElse/cached()/cached(Supplier) 白盒声明。
- `testDefaultResolver`：defaultResolver() 跨调用同实例；String→非空 info（superclass=Object）、Object 自身 superClass=null、Runnable isInterface=true；未知类 → **null**（非 Optional.empty）；**数组 ClassDesc（`[Ljava/lang/String;`/`[I`）→ null**（只认 class-or-interface）；getClassInfo(null) → NPE（消息含 `"cd" is null`）。
- `testStaticHierarchy`：of(interfaces, map) 自造层级（Integer→Number→Object、String→Object）逐项回读 toString；map 外未知 → null、Object 无条目仍已知；interfaces 集合成员 isInterface=true/superclass=Object；info 值语义相等（跨调用 equals）。
- `testComposition`：orElse 主命中/备命中/双缺 → null 三态 + orElse(null) NPE；cached() 每次新实例 + 语义透传；cached(Supplier) **懒加载且恰调用一次**（计数器）+ **supplier 必须给可变 Map（缓存写入 computeIfAbsent，Map.of() → UOE）**。
- `testOtherFactories`：ofClassLoading(ClassLoader)/ofClassLoading(Lookup)/ofResourceParsing(ClassLoader) 命中+miss；lambda resolver 直达。
- `testVerifyClosedLoop`（真闭环）：build `a.Child extends 虚拟 a.Parent`（areturn 触发子类型检查）→ 默认 resolver verify 1 错（消息含 "Parent"）；注入自造 {Child→Parent, Parent→Object} 经 `ClassFile.ClassHierarchyResolverOption.of(res)` → verify 干净；option.classHierarchyResolver()==res。
