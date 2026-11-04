# javax.swing.Popup 设计案

## 1. API 功能
`Popup` 表示弹出的窗口。方法分组：
- 显隐：`show/hide`。

## 2. Java 用户如何使用
用户通常通过 `PopupFactory` 获取 Popup：
```java
Popup popup = PopupFactory.getSharedInstance().getPopup(owner, contents, x, y);
popup.show();
popup.hide();
```
典型链路：工厂 `getPopup` → `show/hide`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/Popup.java` 定义 Popup 及其 `HeadlessPopup` 内部类（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/Popup.java`）。
- `PopupFactory` 返回具体 Popup 子类实例。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验普通类（构造器 protected）。
- 行为（黑盒）：`testBlackBox()` 通过 `PopupFactory` 获取非空 Popup 并调用 `hide()`（headless 下安全）。
