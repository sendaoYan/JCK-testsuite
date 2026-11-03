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
