# javax.swing.plaf.basic.DefaultMenuLayout — DESIGN

## 1. API 功能
`BoxLayout` 的 `UIResource` 子类：`DefaultMenuLayout(Container,int)` 构造、`preferredLayoutSize`。

## 2. Java 用户如何使用
由 `BasicMenuBarUI`/`BasicPopupMenuUI` 作为菜单布局使用；测试构造后断言 LayoutManager2 与尺寸方法。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/basic/DefaultMenuLayout.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`DefaultMenuLayoutTest.testBlackBox`）
构造 `DefaultMenuLayout(target, X_AXIS)` → UIResource/LayoutManager2 标记 → preferredLayoutSize/minimumLayoutSize 非空。
