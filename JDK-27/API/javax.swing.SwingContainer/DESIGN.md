# javax.swing.SwingContainer 设计案

## 1. API 功能
`SwingContainer` 注解标记 Swing 容器类。方法分组：
- 属性：`value()`（默认 true）、`delegate()`（默认空串）。

## 2. Java 用户如何使用
用户用它标注自定义容器以便工具识别：
```java
@SwingContainer
public class MyPanel extends JPanel { }
```
典型链路：`@SwingContainer` → `getAnnotation(SwingContainer.class).value()`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/SwingContainer.java` 定义该注解（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/SwingContainer.java`）。
- Bean 工具（如 NetBeans Matisse）识别该注解。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验注解接口继承 `Annotation`。
- 行为（黑盒）：`testBlackBox()` 用两个被标注的内部类验证默认 `value=true`、`delegate=""` 与自定义值。
