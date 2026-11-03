# java.awt.font.TextHitInfo 设计案

## 1. API 功能分组
- **静态工厂**：`leading(int)`/`trailing(int)`/`beforeOffset(int)`/`afterOffset(int)`。
- **查询**：`getCharIndex()/isLeadingEdge()/getInsertionIndex()`。
- **变换**：`getOtherHit()`（插入点另一侧）、`getOffsetHit(int delta)`（偏移索引）。
- **工具**：`equals/hashCode/toString`。

## 2. Java 用户如何使用
表示文本中一个命中点（字符的 leading/trailing 边），用于光标定位：`TextLayout.hitTestChar(x, y)` 返回 TextHitInfo；`hit.getInsertionIndex()` 得插入位置；`hit.getOtherHit()` 用于文本选择边界翻转。

## 3. 开源代码实际用法调研
- `TextLayout.hitTestChar/getNextRightHit/getNextLeftHit` 返回/消费 TextHitInfo（`src/java.desktop/share/classes/java/awt/font/TextLayout.java`）。
- `test/jdk/java/awt/font/TextLayout/` 光标命中测试用 leading/trailing/beforeOffset/afterOffset 构造命中点断言（本用例工厂语义参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`TextHitInfoTest.testBlackBox()`）：
1. leading/trailing/beforeOffset/afterOffset 的 char/edge/insertion 语义。
2. getOtherHit 翻转到插入点另一侧；getOffsetHit 平移索引。
3. equals/hashCode（==charIndex）/toString 精确格式 "[nL]"/"[nT]"。
白盒（`testWhiteBox()`）：public final、5 个工厂/查询/变换方法签名。
