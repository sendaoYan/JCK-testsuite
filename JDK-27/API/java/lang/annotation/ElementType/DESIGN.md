# java.lang.annotation.ElementType — 设计方案

## 1. API 功能
枚举：`@Target` 的取值域，即注解可应用的程序元素种类：
- 12 个常量（JDK 27 声明顺序）：TYPE, FIELD, METHOD, PARAMETER, CONSTRUCTOR, LOCAL_VARIABLE, ANNOTATION_TYPE, PACKAGE, TYPE_PARAMETER, TYPE_USE, MODULE, RECORD_COMPONENT
- 枚举机制：`values()`/`valueOf(String)`

## 2. Java 用户如何使用
```java
@Target({ElementType.METHOD, ElementType.TYPE_PARAMETER})
@interface Check {}
for (ElementType t : ElementType.values()) { ... }
ElementType.valueOf("FIELD"); // 反序列化/配置解析
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/annotation/ElementType.java`：常量按引入版本排列（MODULE since 9、RECORD_COMPONENT since 16）。
- Spring `@Target(ElementType.TYPE)` 组合极常见（`@Component` 等）；JUnit 5 `@Target({ElementType.TYPE, ElementType.METHOD})`。
- MyBatis `@Select` 等使用 `@Target(ElementType.METHOD)`。

## 4. 测试用例设计 + 自动化实现
白盒：枚举、父类 Enum、字段/方法形态、`getEnumConstants` 非空。
黑盒：
- `values()` 精确 12 个常量及声明顺序（join 字符串比对）；
- `values()` 返回防御性拷贝（改副本不影响原值）；
- 全体常量 `valueOf` 往返；未知名抛 `IllegalArgumentException`；
- 代表性 ordinal（TYPE=0、PACKAGE=7、MODULE=10、RECORD_COMPONENT=11）；
- compareTo/name/toString/equals 枚举机制；MODULE/RECORD_COMPONENT 存在性。
对应 `ElementTypeTest.java` 的 `testBlackBox()`。
