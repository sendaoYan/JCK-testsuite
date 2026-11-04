# javax.swing.RootPaneContainer 设计案

## 1. API 功能
`RootPaneContainer` 接口定义带 `JRootPane` 的顶层/内部容器。方法分组：
- 内容/根：`getContentPane/setContentPane/getRootPane/setRootPane`。
- 分层/玻璃：`getLayeredPane/setLayeredPane/getGlassPane/setGlassPane`。

## 2. Java 用户如何使用
`JFrame`/`JDialog`/`JInternalFrame` 实现该接口：
```java
RootPaneContainer c = internalFrame;
c.setContentPane(new JPanel());
JRootPane root = c.getRootPane();
```
典型链路：`getContentPane/getRootPane` → `setContentPane/setGlassPane`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JInternalFrame.java` 实现 `RootPaneContainer`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JInternalFrame.java`）。
- `JFrame`/`JDialog` 同样实现该接口。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验接口。
- 行为（黑盒）：`testBlackBox()` 用 `JInternalFrame`（headless 安全的轻量容器）覆盖四个 pane 的 get/set。
