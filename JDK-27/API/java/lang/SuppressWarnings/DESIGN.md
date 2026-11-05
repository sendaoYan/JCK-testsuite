# SuppressWarnings — 设计方案

## 1. API 功能
`java.lang.SuppressWarnings`（注解）抑制编译器警告：`@Retention(SOURCE)`、`@Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, MODULE})`，成员 `String[] value()` 列出警告类别（如 "unchecked"、"deprecation"）。

## 2. Java 用户如何使用
- 在无法避免警告的代码处加 `@SuppressWarnings("unchecked")`，告知编译器忽略该类警告。
```java
@SuppressWarnings("unchecked")
List<String> xs = (List) raw;
```

## 3. 开源代码实际用法调研
- JDK 及框架在泛型桥接/反序列化处大量使用；`test/jdk/java/lang/annotation` 验证 SOURCE 保留与 value 类型。

## 4. 测试用例设计 + 自动化实现
白盒：验证为注解类型、`@Retention(SOURCE)`、`@Target` 含 TYPE。
黑盒：反射验证 `value()` 返回 `String[]`。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。

## 契约深化（2026-11）

### API 功能（新增覆盖维度）
**Retention 实测为 SOURCE（brief 所述 RUNTIME 有误，以实测为准）**：所有标注处运行时不可见（TYPE/METHOD/FIELD/CONSTRUCTOR/PARAMETER 五目标逐一断言），并以自造 RUNTIME 注解对照可见性；value() 无默认值；@Target 27 侧不存在（全目标）；数组防御拷贝对照组。

### 参考来源
JLS 9.6.4.2；javap -public 双侧：8 侧带 @Target({TYPE,FIELD,METHOD,PARAMETER,CONSTRUCTOR,LOCAL_VARIABLE})，**27 侧无 @Target**（9 起移除以允许 module 等新目标）。

### 测试用例设计 + 易错规范细节
- **SOURCE retention 意味着任何 @SuppressWarnings 站点运行时 getAnnotation 均为 null**——用自造 @Retention(RUNTIME) 对照注解证明同文件内 RUNTIME 注解可见。
- **@Target 版本差异（核心差异）**：8 侧 6 值、27 侧无 @Target（getAnnotation(Target.class)==null）；两侧各自锁定（MODERN 分支）。
- value() 的 getDefaultValue()==null（使用必须显式给值）；返回 String[]。
- 注解代理数组成员防御拷贝：改 value() 返回数组不影响二次回读（由对照组注解演示，SuppressWarnings 本身运行时不可得实例）。
- SuppressWarningsTest.java 现有 PASS 断言：35。
