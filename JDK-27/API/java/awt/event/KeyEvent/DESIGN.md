# java.awt.event.KeyEvent 行为测试设计

## 1. API 功能
键盘事件：keyCode、keyChar、keyLocation，静态 getKeyText/getKeyModifiersText/getExtendedKeyCodeForChar，isActionKey 判断功能键，大量 VK_* 常量。

## 2. Java 用户如何使用
`new KeyEvent(component, KEY_PRESSED, when, mods, VK_A, CHAR_UNDEFINED)`；KeyListener 读 getKeyCode/getKeyChar/isActionKey。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/KeyEvent.java`（getKeyText/isActionKey/扩展键码）；`test/jdk/java/awt/event/KeyEvent/`。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 InputEvent、常量/构造器、getKeyText 静态。黑盒：构造→回读 keyCode/keyChar/keyLocation/when；setKeyCode/setKeyChar/setModifiers；KEY_TYPED 校验；isActionKey（VK_A false、VK_F5 true）；VK_* 常量精确值；getKeyText 映射；getKeyModifiersText；getExtendedKeyCodeForChar；paramString。
对应 `KeyEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。
