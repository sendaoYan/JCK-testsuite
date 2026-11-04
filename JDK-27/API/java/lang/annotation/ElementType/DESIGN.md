# java.lang.annotation.ElementType — 设计方案（JDK 27）

## 1. API 功能
枚举：`@Target` 取值域。
- 12 个常量（JDK 27 声明顺序）：TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE, MODULE, RECORD_COMPONENT
- 枚举机制：`values()`/`valueOf(String)`

## 2. Java 用户如何使用
```java
@Target({ElementType.METHOD, ElementType.TYPE_PARAMETER}) @interface Check {}
for (ElementType t : ElementType.values()) { ... }
ElementType.valueOf("FIELD");
```

## 3. 开源代码实际用法调研
- JDK `ElementType.java`：MODULE since 9、RECORD_COMPONENT since 16。
- Spring/JUnit/MyBatis 组合使用。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、父类 Enum、getEnumConstants 非空、values/valueOf 存在。
黑盒：`values()` 精确 12 常量及声明顺序；防御性拷贝；全体 valueOf 往返；未知名抛 IAE；每个常量 ordinal == 下标（TYPE=0、PACKAGE=7、MODULE=10、RECORD_COMPONENT=11）；compareTo/name/toString/equals/==；MODULE/RECORD_COMPONENT 存在；switch 覆盖 12 常量。

## 易错规范细节（JDK 27）
- JDK 27 = 12 常量（含 MODULE、RECORD_COMPONENT）；JDK 8 = 10 常量（无二者）。双侧各自断言。
