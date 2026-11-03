# javax.swing.FocusManager 设计案

## 1. API 功能
`FocusManager` 是 Swing 焦点管理抽象类，继承 `java.awt.DefaultKeyboardFocusManager`。方法分组：
- 常量：`FOCUS_MANAGER_CLASS_PROPERTY`。
- 静态：`getCurrentManager()`、`setCurrentManager(FocusManager)`、`disableSwingFocusManager()`、`isFocusManagerEnabled()`。

## 2. Java 用户如何使用
用户获取当前 Swing 焦点管理器：
```java
FocusManager fm = FocusManager.getCurrentManager();
boolean enabled = FocusManager.isFocusManagerEnabled();
```
典型链路：`getCurrentManager` → `isFocusManagerEnabled`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/FocusManager.java` 定义默认焦点管理器创建逻辑（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/FocusManager.java`）。
- JDK 源码 `javax/swing/plaf/basic/BasicLookAndFeel.java` 初始化时安装焦点管理器。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验 abstract、父类 `DefaultKeyboardFocusManager`。
- 行为（黑盒）：`testBlackBox()` 覆盖属性常量、`isFocusManagerEnabled` 默认 true、`getCurrentManager` 非空且稳定。
