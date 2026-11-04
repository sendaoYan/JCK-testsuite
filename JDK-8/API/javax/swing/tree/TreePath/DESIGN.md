# javax.swing.tree.TreePath 测试设计

## 1. API 功能
不可变树路径（Serializable）：`TreePath(Object)`（单节点）与 `TreePath(Object[])`（多段，防御性复制）、
getPathCount/getPathComponent/getLastPathComponent/getParentPath/pathByAddingChild/isDescendant/equals/hashCode/toString。

## 2. Java 用户如何使用
`new TreePath(new Object[]{root, child, leaf})`；`tree.expandPath(p)`、`p.pathByAddingChild(next)`
逐步下钻；`child.isDescendant(ancestorPath)` 判定包含关系。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/tree/TreePath.java`；
`test/jdk/javax/swing/JTree/bug4210432.java`、`DnD/LastNodeLowerHalfDrop.java`。

## 4. 测试用例设计 + 易错细节
- 防御性：构造后改入参数组、改 `getPath()` 返回数组均不影响路径。
- 构造校验（IllegalArgumentException 精确消息）：null → "path in TreePath must be non null."；
  空数组 → "path in TreePath must be non null and not empty."；含 null 元素 →
  "Last path component must be non-null"（父构造路径）。
- `pathByAddingChild(null)` 抛 NullPointerException（"Null child not allowed"）。
- `getPathComponent` 越界/负数抛 IllegalArgumentException（JDK-8 为 ArrayIndexOutOfBoundsException 的
  反例不存在——两侧均 IllegalArgumentException，按实际探测断言）。
- equals 跨构造器（单节点构造 == 等价单元素数组构造）、hashCode 取最后元素 hash、
  toString 形如 "[a, b]"。
- `isDescendant` 方向：a.isDescendant(b) 为 b 在 a 之下；自身算后代。
- 序列化 round-trip equals 保持。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动。
