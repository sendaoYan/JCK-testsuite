# java.awt.font.TransformAttribute 设计案

## 1. API 功能分组
- **类型**：`TransformAttribute`（final）implements Serializable。
- **常量**：`IDENTITY`。
- **构造**：`TransformAttribute(AffineTransform)`。
- **查询**：`getTransform()`（防御性副本）、`isIdentity()`；`hashCode/equals`。

## 2. Java 用户如何使用
作为 `TextAttribute.TRANSFORM` 的属性值，给文本施加仿射变换（旋转/缩放/斜切）：`map.put(TextAttribute.TRANSFORM, new TransformAttribute(AffineTransform.getRotateInstance(Math.PI/2)));`。恒等变换用 `TransformAttribute.IDENTITY`。

## 3. 开源代码实际用法调研
- `java.awt.font.TransformAttribute` 是 TextAttribute.TRANSFORM 的取值类型（`src/java.desktop/share/classes/java/awt/font/TransformAttribute.java`）。
- `test/jdk/java/awt/font/TextLayout/` 有 TRANSFORM 属性用例验证文本变换（本用例 getTransform/isIdentity/equals 参考）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`TransformAttributeTest.testBlackBox()`）：
1. IDENTITY 的 isIdentity/getTransform；缩放变换的 isIdentity/getTransform。
2. getTransform 返回独立副本；显式恒等变换归为 IDENTITY。
3. equals/hashCode 契约。
白盒（`testWhiteBox()`）：public final、implements Serializable、getTransform/isIdentity 签名。
