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

## 5. 契约深化（本轮追加）
- 构造校验：KEY_TYPED + CHAR_UNDEFINED → IAE("invalid keyChar")；id 无校验。
- VK 常量表：ENTER=10/BACK_SPACE=8/TAB=9/ESCAPE=27/SPACE=32/SHIFT=16/CONTROL=17/ALT=18/META=157/UNDEFINED=0/F1=112/F12=123/A=65/Z=90/0=48/9=57/NUMPAD0=96。
- KEY_LOCATION_STANDARD/LEFT/RIGHT=1/2/3。
- getKeyText：Enter/Space/F1 精确；未知键 `Unknown keyCode: 0x...`。
- getKeyModifiersText：Ctrl+Shift、Meta+Alt 拼接顺序。
- getExtendedKeyCodeForChar：非 ASCII 字符返回 0。
- paramString：KEY_TYPED 的 keyCode=0/keyChar='z'；modifiers=/extModifiers= 双呈现。
- 序列化：keyCode/keyLocation/modifiersEx/isShiftDown 保留。
