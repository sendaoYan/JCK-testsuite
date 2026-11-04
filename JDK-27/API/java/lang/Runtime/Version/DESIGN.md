# Runtime.Version — 设计方案

## 1. API 功能
`java.lang.Runtime.Version`（final，实现 `Comparable<Version>`，无公开构造器）按 JEP 223 语义解析版本字符串：静态 `parse(String)`；号段访问 `feature/interim/update/patch` 与旧别名 `major/minor/security`；`version()` 返回号段 `List<Integer>`；可选部分 `pre()/build()/optional()`；比较 `compareTo/compareToIgnoreOptional/equals/equalsIgnoreOptional/hashCode/toString`。

## 2. Java 用户如何使用
- 解析与比较 JDK 版本字符串（含 pre/build）：
```java
if (Runtime.Version.parse(System.getProperty("java.version")).feature() >= 27) { ... }
```
- `Runtime.version()` 取当前运行时版本。

## 3. 开源代码实际用法调研
- Gradle toolchain / Jib / 多版本库以 `Runtime.Version.parse` 判 JDK 特性；JDK `test/jdk/java/lang/Runtime/VersionTest.java` 系统覆盖号段、pre/build、比较与非法输入。

## 4. 测试用例设计 + 自动化实现
白盒：验证 final、实现 Comparable、无公开构造器、`parse` public static、`version()` 返回 List、`pre/build` 返回 Optional、全部无参访问器齐备。
黑盒：`27.0.1` 的 feature/interim/update/patch 与 major/minor/security 别名、`version()` 列表、toString 往返；`+5`/`-ea` 的 pre/build；非法串（abc、27..1、27.0.1++5）抛 IAE/NFE；排序规则（短版本在前、release > pre、build 数值序）；equals/hashCode 契约与 `Runtime.version()` 为 27+。
对应 `VersionTest.java` 白盒 + 黑盒部分。
