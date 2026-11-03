# javax.management.InvalidAttributeValueException 测试设计

## 1. API 功能
`InvalidAttributeValueException` 是 JMX 受检异常，继承 `OperationsException`。构造器：无参、`(String message)`。
作为 `JMException` 族成员，被 `MBeanServer`/`MBeanServerConnection` 各操作在失败时抛出，承载可读 detail message。

## 2. Java 用户如何使用
```java
try {
    mbeanServer.getAttribute(name, "Missing");
} catch (InvalidAttributeValueException e) {
    String why = e.getMessage(); // 详情消息，可能为 null（无参构造）
}
```
构造组合：`new InvalidAttributeValueException("detail")` 保留消息；`new InvalidAttributeValueException()` 消息为 null；`new InvalidAttributeValueException((String)null)` 消息同样为 null。

## 3. 开源代码实际用法调研
来源：JDK 源码 `~/git/jdk25u-dev/src/java.management/share/classes`（检索该类型出现位置）与 Javadoc 契约；不依赖外部 GitHub 项目。
该类在 JMX 实现中被大量抛出（如 `MBeanServer.getAttribute`/`setAttribute` 在属性缺失/非法时、`registerMBean` 重复注册时），
JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/management` 亦通过 `catch (InvalidAttributeValueException e)` 校验这些失败路径。

## 4. 测试用例设计
- 白盒：`Class.forName` 校验 public/非接口/非抽象、直接父类为 `OperationsException`、可序列化、存在无参与 `(String)` 构造器。
- 黑盒：`new InvalidAttributeValueException("fixed-message")` 的 `getMessage()` 精确往返；无参与 `(String)null` 构造 `getMessage()` 为 null；`toString()` 含类名；`instanceof` 继承链校验。
- 确定性：固定输入字符串；只断言规范/实现稳定行为，不断言运行时生成的消息文案。
