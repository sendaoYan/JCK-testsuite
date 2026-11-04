# javax.swing.ToolTipManager 设计案

## 1. API 功能
`ToolTipManager` 全局管理工具提示。方法分组：
- 实例：`sharedInstance`。
- 开关：`setEnabled/isEnabled/setLightWeightPopupEnabled/isLightWeightPopupEnabled`。
- 延迟：`setInitialDelay/getInitialDelay/setDismissDelay/getDismissDelay/setReshowDelay/getReshowDelay`。
- 注册：`registerComponent/unregisterComponent`。

## 2. Java 用户如何使用
用户获取共享管理器配置提示行为：
```java
ToolTipManager.sharedInstance().setInitialDelay(500);
ToolTipManager.sharedInstance().setEnabled(true);
```
典型链路：`sharedInstance()` → `setEnabled/setInitialDelay`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/ToolTipManager.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ToolTipManager.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 final、父类 `MouseAdapter` + `MouseMotionListener`。
- 行为（黑盒）：`testBlackBox()` 覆盖 `sharedInstance` 单例、启用开关、三个延迟 get/set、组件注册/注销。
