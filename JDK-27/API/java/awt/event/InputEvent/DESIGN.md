# java.awt.event.InputEvent 行为测试设计

## 1. API 功能
所有输入事件（键盘/鼠标）抽象基类，带 when 时间戳、modifiers 新旧两套修饰键、isShiftDown/isControlDown/等状态查询、getModifiersEx/getModifiersExText、getMaskForButton。

## 2. Java 用户如何使用
不直接实例化；经 KeyEvent/MouseEvent 使用，读 getWhen/getModifiersEx/isShiftDown 判断输入状态。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/InputEvent.java`（掩码常量、getModifiersEx 位运算）；`test/jdk/java/awt/event/InputEvent/`。

## 4. 测试用例设计 + 自动化实现
白盒：public abstract、继承 ComponentEvent、掩码/方法形态。黑盒：经 KeyEvent 实例化→getWhen/getModifiers/getModifiersEx 回读；isShiftDown/isControlDown/isAltDown 组合；consume/isConsumed；9 个掩码常量精确值；getMaskForButton(1..3) 与 (0/4) 抛 IAE；getModifiersExText。
对应 `InputEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。

## 5. 契约深化（本轮追加）
- 全部掩码精确值：旧 SHIFT/CTRL/META/ALT/BUTTON1=1/2/4/8/16、ALT_GRAPH=32、BUTTON2_MASK==ALT_MASK、BUTTON3_MASK==META_MASK；扩展 SHIFT/CTRL/META/ALT/B1/B2/B3/ALTGRAPH=0x40/0x80/0x100/0x200/0x400/0x800/0x1000/0x2000，两两位不重叠。
- getModifiers()：扩展掩码 → 旧掩码映射（CTRL|ALT_DOWN_MASK → CTRL_MASK|ALT_MASK）。
- getModifiersExText：拼接顺序 Meta+Ctrl+Alt+Shift(+Alt Graph)、Button1/2/3、位 20 渲染 Button10。
- getMaskForButton(0/1000) → IAE；1/2/3 → BUTTON*_DOWN_MASK。
- 序列化：when/getModifiersEx/isXxxDown 保留。
