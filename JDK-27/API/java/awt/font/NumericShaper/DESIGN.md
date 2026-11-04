# java.awt.font.NumericShaper 设计案

## 1. API 功能分组
- **常量**：范围位 `EUROPEAN/ARABIC/EASTERN_ARABIC/DEVANAGARI/.../ALL_RANGES`。
- **工厂**：`getShaper(int)`/`getShaper(Range)`（非上下文）、`getContextualShaper(int)`/`getContextualShaper(int,int)`/`getContextualShaper(Set<Range>)`/`getContextualShaper(Set<Range>,Range)`（上下文）。
- **整形**：`shape(char[],start,count)`、`shape(char[],start,count,int context)`、`shape(char[],start,count,Range context)`。
- **查询**：`isContextual/getRanges/getRangeSet`；`hashCode/equals/toString`。

## 2. Java 用户如何使用
`NumericShaper.getShaper(NumericShaper.ARABIC).shape(text, 0, len)` 把 ASCII 数字转阿拉伯-印度数字；上下文 shaper 根据邻近文字自动选择数字形态。用于多语言文本渲染。

## 3. 开源代码实际用法调研
- `java.awt.font.NumericShaper` 是 JDK 标准 API（`src/java.desktop/share/classes/java/awt/font/NumericShaper.java`），`shape` 把 '0'-'9' 映射到各书写系统数字。
- `test/jdk/java/awt/font/NumericShaper/` 系列用例验证各范围数字整形与上下文选择（本用例 ARABIC/EASTERN_ARABIC 整形参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`NumericShaperTest.testBlackBox()`）：
1. getShaper 非上下文、getRanges 含目标位；ARABIC 把 "0123456789" 转阿拉伯-印度数字（精确）。
2. shape 子区间只改范围内字符；contextual shaper isContextual、getRangeSet 非空、默认上下文整形。
3. equals/hashCode/toString。
白盒（`testWhiteBox()`）：public final、implements Serializable、工厂与 shape 重载、查询方法签名。
