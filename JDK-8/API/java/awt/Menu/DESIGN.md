# java.awt.Menu 行为测试设计

## 1. API 功能
构造：`Menu()`/`(String)`/`(String,boolean tearOff)`；项管理：`add(MenuItem)/add(String)/insert(String|int)/addSeparator/insertSeparator/remove(int|MenuComponent)/removeAll/getItem/getItemCount/countItems`；isTearOff；父链 getParent。

## 2. Java 用户如何使用
```java
Menu m = new Menu("File");
m.add(new MenuItem("Open"));
m.insert("Recent", 1);
```
构造 → 增删插矩阵 → 分隔符本质 → 破坏性反例。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Menu.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractMenu()`）。
对应 `MenuTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- `Menu(null)` 保留 null；`Menu()` 默认空串；tearOff 需显式 ctor 参数。
- `add(MenuItem null)` 抛 NPE 且不清空已有项；`add(String null)` 被接受（label 为 null 的项）。
- 分隔符本质：addSeparator/insertSeparator 就是插入 label 为 "-" 的普通 MenuItem。
- `getItem(99)`/`remove(99)`/`remove(0)` on empty 抛精确 AIOOBE；`insert(String,-1)`/`insertSeparator(-1)` 抛 IAE；insert 越界索引一律追加。
- 破坏性反例锁定：`insert((MenuItem) null, 0)` 抛 NPE **并且清空 items 向量**（count 5→0）——历史副作用，两个 JDK 一致，用一次性 wipe 菜单验证以免污染主链。
- 版本差异：`Menu.add` 会 wire 子项 parent；`Menu.remove(MenuComponent)` 在 JDK 9+ 解除子项 parent 链接，JDK 8 保留 stale 链接（双侧分别断言）。
