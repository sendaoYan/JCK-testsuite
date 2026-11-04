# DocumentEvent.EventType 行为测试设计

## 1. API 功能
`javax.swing.event.DocumentEvent$EventType` 是文档事件类型值类（final class，非枚举），构造器私有，只暴露三个静态实例。

- **静态实例**：`INSERT`、`REMOVE`、`CHANGE`，各持有一个 `typeString`。
- **查询**：`toString()` 返回 `"INSERT"`/`"REMOVE"`/`"CHANGE"`。

## 2. Java 用户如何使用
作为 `DocumentEvent.getType()` 的返回值进行比较（使用 `==` 比较身份）：

```java
if (e.getType() == DocumentEvent.EventType.INSERT) { ... }
else if (e.getType() == DocumentEvent.EventType.REMOVE) { ... }
else if (e.getType() == DocumentEvent.EventType.CHANGE) { ... }
```

每个方法典型调用：`DocumentEvent.EventType.INSERT` 静态引用 → `==` 身份比较 → 需要可读名称时 `toString()`。

## 3. 开源代码实际用法调研
1. `jdk25u-dev/src/java.desktop/share/classes/javax/swing/text/AbstractDocument.java:631/751`：
   ```java
   new DefaultDocumentEvent(offs, len, DocumentEvent.EventType.REMOVE);
   new DefaultDocumentEvent(offs, str.length(), DocumentEvent.EventType.INSERT);
   ```
   将 `EventType.INSERT/REMOVE` 作为构造参数传入，是类型值类的典型消费方式。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 Object、非接口/注解/枚举、public、非 abstract），新增 `final` 修饰符断言。
- 黑盒：验证三个静态实例非空、`toString()` 精确值、两两身份互异、静态引用稳定性（`EventType.INSERT == 局部变量 insert`）。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
