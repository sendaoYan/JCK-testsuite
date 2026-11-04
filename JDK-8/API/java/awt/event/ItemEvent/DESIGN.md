# java.awt.event.ItemEvent 行为测试设计

## 1. API 功能
可选项状态变化事件：ITEM_STATE_CHANGED=701，stateChange SELECTED=1/DESELECTED=2，带 item 对象与 ItemSelectable 源。

## 2. Java 用户如何使用
`new ItemEvent(selectable, ITEM_STATE_CHANGED, item, SELECTED)`；ItemListener 读 getItem/getStateChange/getItemSelectable。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/event/ItemEvent.java`；`Checkbox/Choice/List` 触发。

## 4. 测试用例设计 + 自动化实现
白盒：public、继承 AWTEvent、常量/构造器。黑盒：构造→回读 id/source/item/stateChange/getItemSelectable；DESELECTED；5 个常量精确值；paramString 含 id/stateChange；null item。
对应 `ItemEventTest.java` 的 `testWhiteBox()` / `testBlackBox()`。

## 5. 契约深化（本轮追加）
- SELECTED/DESELECTED=1/2、任意 stateChange 值无校验回读；null item 合法。
- 消费语义：ITEM_STATE_CHANGED 语义 id 构造即 consumed。
- 序列化：id/item/stateChange 保留。
