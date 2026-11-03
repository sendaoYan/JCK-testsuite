# java.awt.geom.IllegalPathStateException 设计案

## 1. API 功能分组
- **异常类型**：`IllegalPathStateException extends RuntimeException`，非受检。
- **构造**：无参与 `IllegalPathStateException(String)`。

## 2. Java 用户如何使用
在路径定义 API（如 `Path2D.lineTo/quadTo/curveTo/closePath`）在缺少初始 `moveTo` 的情况下被调用时抛出，调用方捕获后给出错误提示。构造带描述消息：`throw new IllegalPathStateException("missing initial moveto in path definition")`。

## 3. 开源代码实际用法调研
- JDK 自身 `Path2D`（`src/java.desktop/share/classes/java/awt/geom/Path2D.java`）在 `needRoom`/`closePath` 等处在 `numTypes==0` 且需要 moveTo 时抛 `new IllegalPathStateException("missing initial moveto in path definition")`（源码 354/1243 行附近）。
- `test/jdk/java/awt/geom/Path2D/` 若干用例以 `try { ... } catch (IllegalPathStateException expected) {}` 断言该异常触发条件。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`IllegalPathStateExceptionTest.testBlackBox()`）：
1. 无参构造 message/cause 为 null；String 构造 message 精确保留。
2. 异常 instanceof RuntimeException（非受检）。
3. 真实触发：`new Path2D.Double().lineTo(1,2)` 在无 moveTo 时抛该异常，消息含 "moveto"。
白盒（`testWhiteBox()`）：public class、extends RuntimeException、无参 + String 构造存在。
