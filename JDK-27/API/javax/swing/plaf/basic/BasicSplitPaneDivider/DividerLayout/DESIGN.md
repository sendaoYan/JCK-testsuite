# javax.swing.plaf.basic.BasicSplitPaneDivider.DividerLayout — DESIGN

## 1. API 功能
`BasicSplitPaneDivider` 的内部类，实现 `LayoutManager`（构造器 protected）。

## 2. Java 用户如何使用
构造器为 protected/包私有，无法从测试默认包直接实例化；测试以反射断言其构造器均非 public，并校验父类/接口/成员方法形状。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/BasicSplitPaneDivider.java`（jdk25u-dev）。该类为内部类，仅由宿主 UI 内部创建。

## 4. 测试用例设计 + 自动化实现（`DividerLayoutTest.testBlackBox`）
反射加载 → 构造器均非 public → 父类/接口/成员方法形状断言。
