# Module — 设计方案

## 1. API 功能
`java.lang.Module`（final，实现 `AnnotatedElement`）表示一个模块：
- 标识：`isNamed/getName/getDescriptor/getLayer/getClassLoader`
- 可读性/可访问性：`canRead/addReads/canUse/addUses/isExported/isOpen/addExports/addOpens`
- 资源/包：`getPackages/getResourceAsStream`
- 注解：`getAnnotation/getAnnotations/getDeclaredAnnotations`
- 其他：`isNativeAccessEnabled`

## 2. Java 用户如何使用
- `String.class.getModule()` 获取模块；`isExported` 判断导出（java.lang 已导出）、`isOpen` 判断开放（java.base 未 open）；`getResourceAsStream` 读模块内资源。
```java
Module m = Object.class.getModule();   // java.base
boolean exported = m.isExported("java.lang");
```

## 3. 开源代码实际用法调研
- JDK `ModuleLayer`/`Configuration` 构建时大量操作 `Module`。
- JDK `test/jdk/java/lang/Module` 系列验证 `isExported`、`canRead`、`addReads`、资源读取。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Module")` 验证 final、实现 `AnnotatedElement`。
黑盒：`java.base` 的命名/导出/开放/资源/注解/loader；未命名模块的 `isNamed=false`、name null、可读 `java.base`；`addReads(self)` 返回同模块。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 模块身份闭环：`toString()=="module java.base"`、`getLayer()==ModuleLayer.boot()`、`descriptor().name()` 与 `getName()` 一致、`hashCode()` 为 Object 身份哈希（Module 未覆写）。
- 导出/开放矩阵：`isExported("java.lang")==true`、`jdk.internal.misc`/`jdk.internal.loader` 未导出、`isOpen` 全 false；限定形参 null → NPE。
- 资源：`getResourceAsStream` 接受 `/` 前缀名；`module-info.class` 可读且以 CA FE BA BE 开头；不存在资源返回 null；null 名 NPE。
- 未命名模块：`getDescriptor()/getLayer()` 均为 null、`toString()=="unnamed module @<hash>"`、读写一切模块、`addReads/addExports/addOpens` 为返回 this 的 no-op（null 包名 IAE、null 目标 NPE）。
- 变异方法越权：named 模块外部调用 `addExports/addOpens/addReads/addUses` → IllegalCallerException。

### 2. 使用方式
- 通过 `String.class.getModule()` 取 java.base、通过 `ClassLoader.getUnnamedModule()` 取类路径模块、通过自建层（`defineModulesWithOneLoader` + 空包模块）取得"自己的" named 模块，不触碰真模块的变异面。

### 3. 参考来源
- Oracle javadoc java.lang.Module（addExports 的 IllegalCallerException 条款、unnamed module 语义）。
- OpenJDK `test/jdk/java/lang/Module` 系列的等价类划分思路（自写实现）。

### 4. 测试用例设计 + 易错规范细节
- 新增 `testJavaBaseContract/testUnnamedContract/testOwnNamedModule/testAnnotationContract/testCrossApiContract/testWhiteBoxDeepen`：断言数 36→161。
- 易错点：
  - `Module.getResourceAsStream` 名字是模块相对路径，前导 `/` 也可容忍（实测返回流）；
  - `canRead(null)`/`isExported(null)`/`isOpen(pkg,null)` → NPE；`unnamed.addExports(null, m)` 是 IAE 而非 NPE；
  - requires 决定 canRead：自建双模块层中 B requires A ⇒ B.canRead(A)==true、A.canRead(B)==false；java.base 只读自身；
  - `java.base.isNativeAccessEnabled()==true`（本 JDK-27 运行时实测锁定）；
  - `Module.getPackages().equals(descriptor().packages())` 为 java.base 不变式。
