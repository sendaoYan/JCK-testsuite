# java.awt.AlphaComposite 黑盒行为测试设计

## 1. API 功能
- 常量：规则 `CLEAR/SRC/SRC_OVER/DST_OVER/SRC_IN/DST_IN/SRC_OUT/DST_OUT/SRC_ATOP/DST_ATOP/XOR/DST`；实例 `Clear/Src/SrcOver/.../Xor`。
- 工厂：`getInstance(int rule)`、`getInstance(int rule, float alpha)`。
- 查询：`getRule`、`getAlpha`。
- 派生：`derive(int rule)`、`derive(float alpha)`。
- 绘制：`createContext(ColorModel,ColorModel,RenderingHints)` 返回 `CompositeContext`。
- 值语义：`equals`、`hashCode`。

## 2. Java 用户如何使用
`Graphics2D.setComposite` 设置 alpha 混合规则；透明窗口/图像合成常用 `SrcOver` + alpha。
```java
g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
g2.drawImage(img, 0, 0, null);
```
组合链路：`getInstance(rule,alpha)` → `getRule/getAlpha` → `derive` → `equals` → `createContext`。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/AlphaComposite.java`：`getInstance(rule)` 返回缓存常量；`getInstance(rule, 1.0f)` 也返回常量，否则 `new`；`derive(rule/alpha)` 参数不变时返回 `this`；`equals` 比较 rule + extraAlpha；规则校验 `rule < MIN_RULE || rule > MAX_RULE` 抛 `IllegalArgumentException`，alpha 越界抛 `IllegalArgumentException`。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/Graphics2D/DrawImageIAETest/DrawImageIAETest.java`、`test/jdk/java/awt/Graphics/LCDTextAndGraphicsState.java`（`AlphaComposite.getInstance`）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 规则常量数值（CLEAR=1…XOR=12）。
- 静态实例与规则对应；常量 alpha=1.0。
- `getInstance(rule)` 返回缓存常量；`getInstance(rule,alpha)` 规则/alpha；alpha=1.0 返回常量。
- `derive(rule)`/`derive(alpha)` 相同返回 `this`、不同返回新实例/常量。
- `equals/hashCode`（不同 alpha/rule 不等、null/异类）。
- `createContext` 非空 + `dispose`。
- 非法 rule/alpha 抛 `IllegalArgumentException`。
断言 64 项，字段精确断言（headless-safe）。
