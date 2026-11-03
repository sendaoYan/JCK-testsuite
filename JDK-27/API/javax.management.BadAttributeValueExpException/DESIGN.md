# javax.management.BadAttributeValueExpException 测试设计

## 1. API 功能
`BadAttributeValueExpException` 继承 `Exception`，表示查询构造方法收到了非法 MBean 属性值。
构造器：`(Object val)`；可观察方法：toString()。

## 2. Java 用户如何使用
```java
throw new BadAttributeValueExpException(someValue);
// toString() -> "BadAttributeValueException: " + val
```

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes/javax/management/BadAttributeValueExpException.java` 及 Query 表达式求值路径；
JDK 自带 `test/jdk/javax/management` 的 Query 用例在构造非法查询表达式时触发这些异常。

## 4. 测试用例设计
- 白盒：public/非接口/非抽象、继承 `Exception`、可序列化、构造器与方法形态。
- 黑盒：构造后断言 `toString()` 精确内容、`getMessage()` 为 null（无 detail message）、`instanceof Exception`；。
- 确定性：固定值字符串/对象。
