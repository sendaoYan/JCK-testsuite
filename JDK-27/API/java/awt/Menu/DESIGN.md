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

## ⑤ 契约深化（本轮追加）
- `Menu(null)` 保留 null；`Menu()` 默认空串；tearOff 需显式 ctor 参数。
- `add(MenuItem null)` 抛 NPE 且不清空已有项；`add(String null)` 被接受（label 为 null 的项）。
- 分隔符本质：addSeparator/insertSeparator 就是插入 label 为 "-" 的普通 MenuItem。
- `getItem(99)`/`remove(99)`/`remove(0)` on empty 抛精确 AIOOBE；`insert(String,-1)`/`insertSeparator(-1)` 抛 IAE；insert 越界索引一律追加。
- 破坏性反例锁定：`insert((MenuItem) null, 0)` 抛 NPE **并且清空 items 向量**（count 5→0）——历史副作用，两个 JDK 一致，用一次性 wipe 菜单验证以免污染主链。
- 版本差异：`Menu.add` 会 wire 子项 parent；`Menu.remove(MenuComponent)` 在 JDK 9+ 解除子项 parent 链接，JDK 8 保留 stale 链接（双侧分别断言）。
