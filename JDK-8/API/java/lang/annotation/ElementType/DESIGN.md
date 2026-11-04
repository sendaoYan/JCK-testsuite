# java.lang.annotation.ElementType — 设计方案（JDK 8）

## 1. API 功能
枚举：`@Target` 的取值域（注解可应用的程序元素种类）。
- **JDK 8 恰好 10 个常量**（声明顺序）：TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE
- 枚举机制：`values()`/`valueOf(String)`

## 2. Java 用户如何使用
```java
@Target({ElementType.METHOD, ElementType.TYPE_PARAMETER}) @interface Check {}
for (ElementType t : ElementType.values()) { ... }
ElementType.valueOf("FIELD");
```

## 3. 开源代码实际用法调研
- JDK `ElementType.java`：常量按引入版本排列；JDK 8 无 MODULE/RECORD_COMPONENT。
- Spring `@Target(ElementType.TYPE)`、JUnit 5 `@Target({TYPE, METHOD})` 等。

## 4. 测试用例设计 + 自动化实现
- 结构：枚举、父类 Enum、values/valueOf 存在、getEnumConstants 非空。
- `values()` 精确 10 常量及声明顺序；防御性拷贝。
- 全体常量 valueOf 往返；未知名抛 IllegalArgumentException。
- 每个常量 ordinal == 声明下标；代表性 ordinal（TYPE=0、PACKAGE=7、TYPE_USE=9）。
- compareTo/name/toString/equals/== 枚举机制。

## 易错规范细节（JDK 8）
- **版本差异**：JDK 8 = 10 常量（无 MODULE、RECORD_COMPONENT）；JDK 27 = 12 常量（多 MODULE ordinal=10、RECORD_COMPONENT ordinal=11）。双侧各自断言自己的 `values().length`。
