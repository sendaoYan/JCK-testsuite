# javax.swing.PopupFactory 设计案

## 1. API 功能
`PopupFactory` 工厂创建 `Popup`。方法分组：
- 实例：`setSharedInstance/getSharedInstance`。
- 创建：`getPopup(owner, contents, x, y)`。

## 2. Java 用户如何使用
用户用工厂创建弹出：
```java
Popup popup = PopupFactory.getSharedInstance().getPopup(owner, contents, 0, 0);
```
典型链路：`getSharedInstance()` → `getPopup`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/PopupFactory.java` 是 JDK 自身实现，含 `setPopupType/getPopupType` 内部逻辑（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/PopupFactory.java`）。
- `BasicToolTipUI` 使用 `PopupFactory` 显示提示。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验普通类。
- 行为（黑盒）：`testBlackBox()` 覆盖共享实例单例、`setSharedInstance`、`getPopup` 返回非空（headless 返回 HeadlessPopup）。
