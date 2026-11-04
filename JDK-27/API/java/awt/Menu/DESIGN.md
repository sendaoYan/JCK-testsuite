# java.awt.Menu 黑盒行为测试设计

## ① API 功能分组
- 构造：`Menu()`、`Menu(String)`、`Menu(String, boolean tearOff)`。
- 条目管理：`add(MenuItem)/add(String)/insert/insertSeparator/addSeparator/remove/removeAll/getItemCount/getItem`。
- 查询：`isTearOff`。
- 继承 MenuItem：`getLabel/setLabel/isEnabled/setEnabled`。

## ② Java 用户用法
```java
Menu m = new Menu("File");
m.add(new MenuItem("Open"));
m.addSeparator();
m.add(new MenuItem("Exit"));
menuBar.add(m);
```
组合链路：构造 → add/insert 条目与分隔符 → getItemCount/getItem 读回 → remove/removeAll。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Menu.add(String)` 内部 `add(new MenuItem(label))`（`Menu.java`）。
- `Menu.addSeparator` 添加标签为 `-` 的 MenuItem（`Menu.java`）。
- `Menu.remove(int)` 按索引移除（`Menu.java`）。

## ④ 测试用例设计 + 自动化实现
- 默认标签/tear-off + 构造 tear-off → `testBlackBox`。
- add/add(String)/addSeparator/insert/insertSeparator 顺序与数量 → `testBlackBox`。
- remove(int)/remove(MenuComponent)/removeAll → `testBlackBox`。
- 继承 MenuItem 的 setLabel/setEnabled → `testBlackBox`。
