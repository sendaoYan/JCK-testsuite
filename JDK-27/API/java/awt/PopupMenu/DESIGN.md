# java.awt.PopupMenu 黑盒行为测试设计

## ① API 功能分组
- 构造：`PopupMenu()`、`PopupMenu(String)`。
- 继承 Menu：`add/getItemCount/getItem/getLabel`。
- 弹出：`show(Component, int, int)`、`addNotify`。
- 查询：`getParent`。

## ② Java 用户用法
```java
PopupMenu p = new PopupMenu("context");
p.add(new MenuItem("Cut"));
component.add(p);
p.show(component, x, y);
```
组合链路：构造 → add 条目 → 挂到组件 → show 弹出 → 读回父/条目。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `PopupMenu.show` 创建弹出窗口并在指定坐标显示（`PopupMenu.java`）。
- `Component.add(PopupMenu)` 将弹出菜单挂到组件（`Component.java`）。
- `PopupMenu.getParent` 返回挂载的 MenuContainer（`PopupMenu.java`）。

## ④ 测试用例设计 + 自动化实现
- 默认/构造标签 → `testBlackBox`。
- add/getItemCount/getItem → `testBlackBox`。
- 挂到显示组件后 getParent + show 弹出（Xvfb 无 WM）→ `testBlackBox`。

## ⑤ 契约深化（本轮追加）
- `show(origin, x, y)` 前置条件：origin 非 showing 时抛精确 RuntimeException（非 IllegalComponentStateException 子类）；显示 host 后 show 成功且项可枚举。
- ctor(String) label 保留（含 null）；继承 Menu 的全部增删契约。
