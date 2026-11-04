# java.awt.Label 黑盒行为测试设计

## ① API 功能分组
- 常量：`LEFT/CENTER/RIGHT`。
- 构造：`Label()`、`Label(String)`、`Label(String, int alignment)`。
- 文本/对齐：`getText/setText`、`getAlignment/setAlignment`。

## ② Java 用户用法
```java
Label l = new Label("name", Label.RIGHT);
l.setText("other");
l.setAlignment(Label.CENTER);
panel.add(l);
```
组合链路：构造（含对齐）→ 文本/对齐 setter → add 到显示 Frame。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Label` 构造器默认 `text=""`、`alignment=LEFT`（`Label.java`）。
- `Label.setText` 更新 text 字段（`Label.java`）。
- `Label.setAlignment` 校验 LEFT/CENTER/RIGHT 后更新（`Label.java`）。

## ④ 测试用例设计 + 自动化实现
- 对齐常量精确值 → `testBlackBox`。
- 三构造器默认/指定文本与对齐 → `testBlackBox`。
- setText/setAlignment 往返 → `testBlackBox`。
- add 到显示 Frame 后 displayable/showing → `testBlackBox`。
