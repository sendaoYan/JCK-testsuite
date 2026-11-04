# javax.swing.KeyStroke 设计案

## 1. API 功能
`KeyStroke` 描述按键（含修饰键），是 `InputMap` 的键。方法分组：
- 工厂：`getKeyStroke(String)`、`getKeyStroke(int, int)`、`getKeyStroke(char)`、`getKeyStrokeForEvent(KeyEvent)`。
- 查询：`getKeyChar()`、`getKeyCode()`、`getModifiers()`。

## 2. Java 用户如何使用
用户用字符串形式注册快捷键：
```java
inputMap.put(KeyStroke.getKeyStroke("ctrl S"), "save");
KeyStroke ks = KeyStroke.getKeyStrokeForEvent(keyEvent);
```
典型链路：`getKeyStroke("A")` → `getKeyChar/getKeyCode/getModifiers`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/plaf/basic/BasicLookAndFeel.java` 用 `KeyStroke.getKeyStroke` 构造 `InputMap`（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/basic/BasicLookAndFeel.java`）。
- `InputMap`/`ActionMap` 快捷键体系普遍使用 `KeyStroke`。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `AWTKeyStroke`。
- 行为（黑盒）：`testBlackBox()` 覆盖字符串/虚拟键/修饰键三种构造、`getKeyChar/getKeyCode/getModifiers`、缓存同一性、`getKeyStrokeForEvent`。
