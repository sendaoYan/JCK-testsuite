# Deprecated — 设计方案

## 1. API 功能
`java.lang.Deprecated`（注解）标记已过时 API：`@Retention(RUNTIME)`、`@Target({CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, MODULE, PARAMETER, TYPE})`。Java 9+ 新增成员 `String since()` 与 `boolean forRemoval()`。

## 2. Java 用户如何使用
- 在过时方法/类型上加 `@Deprecated`，编译与 IDE 提示；Java 9+ 用 `@Deprecated(since="9", forRemoval=true)` 标注版本与计划移除。
```java
@Deprecated(since="1.2")
void legacy() {}
```

## 3. 开源代码实际用法调研
- JDK 内部旧 API（`Thread.stop` 等）大量标注 `@Deprecated`；`test/jdk/java/lang/Deprecated` 验证 RUNTIME 保留与成员。

## 4. 测试用例设计 + 自动化实现
白盒：验证为注解类型、`@Retention(RUNTIME)`、`@Target` 含 METHOD/TYPE。
黑盒：自定义方法加 `@Deprecated` 后反射可见；反射探测 `since()/forRemoval()` 的返回类型（JDK 8 缺失则不判）。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

## 契约深化（2026-11）

### API 功能（新增覆盖维度）
since()/forRemoval() 成员契约（默认值/自定义回读/toString 形）、@Target 8 值精确集、@Documented 元注解、多目标标注回读（TYPE/METHOD/FIELD/CONSTRUCTOR/PARAMETER）、注解代理 equals/hashCode/annotationType、真实 JDK API 存在性。

### 使用方式
```java
@Deprecated(since = "9", forRemoval = true)
void gone() {}
// 反射回读：.since()=="9"、.forRemoval()==true
// toString() == "@java.lang.Deprecated(forRemoval=true, since=\"9\")"（成员名字母序）
```

### 参考来源
JDK 9+ javadoc（since/forRemoval）；javap -public 双侧（8 侧空接口、27 侧两成员）。

### 测试用例设计 + 易错规范细节
- **默认值**：since() == ""、forRemoval() == false（getDefaultValue 与实例调用双断言）。
- **代理 toString 成员按名字母序**（forRemoval 在 since 前）。
- @Target 27 侧恰 8 值含 MODULE；8 侧 7 值无 MODULE（MODERN 分支）。
- 真实 API：java.util.Date.getDate() 双侧 @Deprecated；Class.newInstance() 27 侧 @Deprecated(since="9")、8 侧无注解；Object.finalize() 27 侧 @Deprecated(since="9", forRemoval=true)、8 侧无注解（版本差异各侧锁定）。
- 无成员侧（8）代理 hashCode==0；有成员侧只断相等性稳定。
- DeprecatedTest.java 现有 PASS 断言：55。
