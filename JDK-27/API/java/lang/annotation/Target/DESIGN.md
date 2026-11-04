# java.lang.annotation.Target — 设计方案

## 1. API 功能
元注解，限制注解可应用的程序元素：
- 唯一成员 `ElementType[] value()`（无默认；不写 @Target 即任意目标）
- 自身 `@Target(ANNOTATION_TYPE)`、`@Retention(RUNTIME)`

## 2. Java 用户如何使用
```java
@Target(ElementType.FIELD) @interface Column { }
@Target({ElementType.METHOD, ElementType.PARAMETER}) @interface Inject { }
@Column int id;         // 合法；用在类型上则编译错误
Method m = ...; m.getAnnotation(Inject.class); // 运行期读取
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/annotation/Target.java`：自注解 `@Target(ANNOTATION_TYPE)`。
- JPA（Jakarta Persistence）`@Column` 为 `@Target({METHOD, FIELD})`；JUnit `@Test` 为 `@Target({METHOD, TYPE, ...})`。
- Spring `@Autowired` 声明 `@Target({CONSTRUCTOR, METHOD, PARAMETER, FIELD, ANNOTATION_TYPE})`。

## 4. 测试用例设计 + 自动化实现
白盒：注解类型、RUNTIME 保留、单成员 `ElementType[] value()`、自身 `@Target(ANNOTATION_TYPE)`。
黑盒（javac 在编译期强制目标约束，运行期验证正空间 + 无泄漏）：
- `@Target(FIELD)` 注解出现在字段；`@Target({METHOD,PARAMETER})` 出现在方法与形参且成员值可读；
- 无 @Target 注解可用于类型；
- 各注解声明的 `value()` 精确回读（长度、常量、声明顺序）；
- field-only 注解未泄漏到类等非法位置。
对应 `TargetTest.java` 的 `testBlackBox()`。
