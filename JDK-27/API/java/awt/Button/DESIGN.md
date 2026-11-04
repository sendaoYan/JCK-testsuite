# java.awt.Button 黑盒行为测试设计

## ① API 功能分组
- 构造：`Button()`、`Button(String label)`。
- 标签/命令：`getLabel/setLabel`、`getActionCommand/setActionCommand`。
- 事件：`addActionListener/removeActionListener/getActionListeners`。
- 继承 Component：`setEnabled/setBackground/setBounds`。

## ② Java 用户用法
```java
Button b = new Button("OK");
b.setActionCommand("ok");
b.addActionListener(e -> {});
panel.add(b);
```
组合链路：构造 → 设标签/命令 → 添加监听器 → add 到显示 Frame → 状态/几何断言。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Button.setLabel` 更新 label 字段并同步 peer（`Button.java`）。
- `Button.setActionCommand` 存储 actionCommand（`Button.java`）。
- `Button.addActionListener` 维护 actionListener 链（`Button.java`）。

## ④ 测试用例设计 + 自动化实现
- 构造标签/setLabel/setActionCommand 往返 → `testBlackBox`。
- add/remove/getActionListeners 监听器管理 → `testBlackBox`。
- add 到显示 Frame 后 displayable/showing + 使能/背景/几何 → `testBlackBox`。
