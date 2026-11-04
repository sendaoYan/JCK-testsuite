# javax.swing.RepaintManager 设计案

## 1. API 功能
`RepaintManager` 管理组件重绘。方法分组：
- 实例：`currentManager/setCurrentManager`。
- 双缓冲：`isDoubleBufferingEnabled/setDoubleBufferingEnabled`。
- 缓冲尺寸：`getDoubleBufferMaximumSize/setDoubleBufferMaximumSize`。

## 2. Java 用户如何使用
用户获取当前重绘管理器配置双缓冲：
```java
RepaintManager rm = RepaintManager.currentManager(component);
rm.setDoubleBufferingEnabled(false);
```
典型链路：`currentManager` → `setDoubleBufferingEnabled` → `getDoubleBufferMaximumSize`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/RepaintManager.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/RepaintManager.java`）。
- `JComponent.paint` 调用 `RepaintManager` 管理绘制。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验普通类。
- 行为（黑盒）：`testBlackBox()` 覆盖 `currentManager` 单例、双缓冲开关、缓冲最大尺寸默认值（headless 下为 `Integer.MAX_VALUE`，有显示时等于最大屏幕尺寸，均为正数）与设置、`setCurrentManager`（headless 下避免 `getOffscreenBuffer` 返回 null 的路径）。
