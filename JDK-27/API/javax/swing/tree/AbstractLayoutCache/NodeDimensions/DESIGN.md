# javax.swing.tree.AbstractLayoutCache.NodeDimensions 行为化测试设计

## 1. API 功能
`AbstractLayoutCache` 内部的抽象静态类，向布局缓存提供单个节点渲染尺寸。
- 唯一抽象方法：`Rectangle getNodeDimensions(Object value, int row, int depth, boolean expanded, Rectangle placeIn)`。
- 约定：`placeIn` 非 null 时复用并返回之，否则新建；`row` 为行号、`depth` 为层级（根为 0）。

## 2. Java 用户如何使用
匿名/子类实现后交给布局缓存：`cache.setNodeDimensions(new AbstractLayoutCache.NodeDimensions() { public Rectangle getNodeDimensions(...) { r.x = depth*10; r.width = 20; r.height = 15; return r; } });` 再由 `getBounds`/`getPreferredHeight`/`getPreferredWidth` 间接消费。

## 3. 开源代码实际用法调研
- `test/jdk/javax/swing/tree/FixedHeightLayoutCache/bug4210354.java`：`static class DummyNodeDimensions extends AbstractLayoutCache.NodeDimensions { public Rectangle getNodeDimensions(...) { return rectangle; } }`，随后 `fhlc.setNodeDimensions(dim)`。
- JDK 源码 `AbstractLayoutCache#getNodeDimensions(...)`（protected）：`NodeDimensions nd = getNodeDimensions(); if (nd != null) return nd.getNodeDimensions(...); return null;` —— 布局缓存通过它取得节点尺寸。

## 4. 测试用例设计 + 自动化实现
黑盒部分在 `NodeDimensionsTest.java`：
- 匿名实现返回 `x=depth*5`、`width=12`、`height=20`。
- 断言传入 `placeIn` 时复用同一 `Rectangle` 实例并写入正确尺寸；传入 null 时新建非空 `Rectangle`。
