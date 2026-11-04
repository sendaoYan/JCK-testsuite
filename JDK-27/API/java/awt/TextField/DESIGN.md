# java.awt.TextField 黑盒行为测试设计

## ① API 功能分组
- 构造：`TextField()`、`TextField(String)`、`TextField(int)`、`TextField(String, int)`。
- 回显字符：`getEchoChar/setEchoChar/echoCharIsSet`。
- 列：`getColumns/setColumns`、`getPreferredSize(int)`。
- 文本/选择/光标（继承 TextComponent）：`setText/getText/select/selectAll/getSelectedText/getSelectionStart/getSelectionEnd/setCaretPosition/getCaretPosition/setEditable/isEditable`。
- 事件：`addActionListener/removeActionListener/getActionListeners`。

## ② Java 用户用法
```java
TextField t = new TextField("password", 20);
t.setEchoChar('*');
t.select(0, 8); t.selectAll();
```
组合链路：构造 → 列/回显字符 setter → 文本/选择/光标组合 → add 到显示 Frame。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `TextField` 默认 text=""、columns=0（`TextField.java`）。
- `TextField.setEchoChar` 设置回显字符并同步 peer（`TextField.java`）。
- `TextComponent.select` 更新 selectionStart/End（`TextComponent.java`）。

## ④ 测试用例设计 + 自动化实现
- 默认文本/列/回显字符 → `testBlackBox`。
- setText/setColumns/setEchoChar 往返 + echoCharIsSet → `testBlackBox`。
- 选择/全选/光标/可编辑 组合 → `testBlackBox`。
- getPreferredSize/getMinimumSize 非空 → `testBlackBox`。
- ActionListener 管理 → `testBlackBox`。
- add 到显示 Frame 后 displayable → `testBlackBox`。

## ⑤ 契约深化（本轮追加）
- 列宽推导：`TextField("abcde").getColumns()==5`（默认列数取文本长度）；`TextField()`/`TextField(null)` 为 0 与空文本。
- `setColumns(0)` 允许（负数 IAE）；setColumns 往返。
- 回显语义：默认 echoChar=0 且 echoCharIsSet==false；`setEchoChar('x')` 后为真；**NUL echoChar（'\0'）取消回显**（echoCharIsSet 回 false）——清回显的合法路径。
- `setText(null)` 归一化为空串；getSelectedText/getText 不受回显字符影响（回显纯显示层）。
- 同一 ActionListener 重复注册计数 2；getListeners 与显式 getter 一致。
