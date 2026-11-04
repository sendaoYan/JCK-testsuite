# java.awt.AWTKeyStroke 黑盒行为测试设计

## 1. API 功能
- 工厂：`getAWTKeyStroke(char)`、`getAWTKeyStroke(Character,int)`、`getAWTKeyStroke(int,int)`、`getAWTKeyStroke(int,int,boolean)`、`getAWTKeyStroke(String)`、`getAWTKeyStrokeForEvent(KeyEvent)`。
- 查询：`getKeyChar/getKeyCode/getModifiers/isOnKeyRelease/getKeyEventType`。
- 值对象：`equals/hashCode/toString`。

## 2. Java 用户如何使用
表达键盘快捷键（JComponent 的 key binding）：
```java
AWTKeyStroke ks = AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK);
AWTKeyStroke typed = AWTKeyStroke.getAWTKeyStroke('a');
int type = ks.getKeyEventType(); // KEY_PRESSED/KEY_TYPED/KEY_RELEASED
```

## 3. 开源代码实际用法调研
- JDK 源码 `src/java.desktop/share/classes/java/awt/AWTKeyStroke.java`：工厂走 AppContext 缓存共享实例；`getKeyEventType` 由 keyCode 是否 `VK_UNDEFINED` 与 onKeyRelease 决定；`getAWTKeyStroke((Character)null,…)` 抛 `IllegalArgumentException`；字符串解析非法抛 `IllegalArgumentException`。
- `src/java.desktop/share/classes/javax/swing/KeyStroke.java`（Swing 门面）、`javax/swing/InputMap.java` 使用 AWTKeyStroke。
- `test/jdk/javax/swing/KeyStroke/` 断言按键解析与 equals。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 六种工厂的参数回读（char/code/modifiers/release/type）。
- `null` Character/字符串、非法字符串抛 `IllegalArgumentException`。
- 修饰符（SHIFT_DOWN_MASK/CTRL_DOWN_MASK）保留。
- `getAWTKeyStrokeForEvent` 对 KEY_PRESSED/KEY_TYPED 的映射。
- equals/hashCode 契约 + 异类/null。
headless-safe。
