# javax.naming.directory.SchemaViolationException 设计案

## 1. API 功能
异常类（`extends javax.naming.NamingException`）。
- 构造器：`SchemaViolationException(String explanation)`、`SchemaViolationException()`。
- 继承方法：`getMessage()`/（NamingException 的）`getExplanation()`，以及 `getResolvedName()`/`getResolvedObj()`/`getRemainingName()` 等解析状态字段。

## 2. Java 用户如何使用
目录操作失败时由 JNDI Provider 抛出；用户 `catch (SchemaViolationException e)` 后读取 `e.getMessage()`/`e.getExplanation()`。无参构造用于默认消息为 null 的场景。

## 3. 开源代码实际用法调研
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/directory/SchemaViolationException.java`（两个构造器均委托 `NamingException`）。
- 来源：`~/git/jdk25u-dev/src/java.naming/share/classes/javax/naming/NamingException.java`（`getExplanation()` 返回 `getMessage()`，无参时解析字段为 null）。

## 4. 测试用例设计
白盒（Test.testWhiteBox）：public/非接口/extends NamingException + 两个构造器。
黑盒（Test.testBlackBox）：带消息构造（getMessage/getExplanation 相等、instanceof NamingException、解析字段为 null）；无参构造（getMessage/getExplanation 为 null）。
