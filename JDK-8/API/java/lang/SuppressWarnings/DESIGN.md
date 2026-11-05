# SuppressWarnings — 设计方案

## 1. API 功能
`java.lang.SuppressWarnings(String[] value)`（注解）：按名字抑制编译器警告。**@Retention(SOURCE)**——运行时完全不可见。JDK-8 侧 @Target({TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE})（6 值）。

## 2. Java 用户如何使用
```java
@SuppressWarnings({"unused", "deprecation"})
int legacyField;
```

## 3. 参考来源
JLS 9.6.4.5；javap -public 双侧：8 侧带 6 值 @Target、**27 侧无 @Target**（本批实测差异）。

## 4. 测试用例设计 + 易错规范细节
- **Retention 实测 SOURCE（非 RUNTIME）**：五目标标注处 getAnnotation(SuppressWarnings.class) 全部 null；同文件自造 RUNTIME 对照注解可见——对照组证明断言方法正确。
- value() 返回 String[]、**无默认值**（getDefaultValue()==null）、唯一成员。
- @Target 8 侧精确 6 值（EnumSet 断言 + PACKAGE 缺席）；@Documented 不在。
- 注解代理数组成员防御拷贝由对照组注解演示（本注解运行时不可得实例）。
- SuppressWarningsTest.java 现有 PASS 断言：40。
