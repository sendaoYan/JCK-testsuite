# java.awt.BasicStroke 黑盒行为测试设计

## 1. API 功能
- 常量：`JOIN_MITER/JOIN_ROUND/JOIN_BEVEL`、`CAP_BUTT/CAP_ROUND/CAP_SQUARE`。
- 构造：`()`、`(float)`、`(float,int,int)`、`(float,int,int,float)`、`(float,int,int,float,float[],float)`。
- 查询：`getLineWidth`、`getEndCap`、`getLineJoin`、`getMiterLimit`、`getDashArray`（返回 clone）、`getDashPhase`。
- 几何：`createStrokedShape(Shape)`。
- 值语义：`equals`、`hashCode`。

## 2. Java 用户如何使用
`Graphics2D.setStroke` 定义描边样式（线宽/端点/连接/虚线）。
```java
BasicStroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND, 10.0f, new float[]{4.0f, 2.0f}, 0.0f);
g2.setStroke(dashed);
g2.draw(shape);
```
组合链路：构造（含 dash）→ `getDashArray` → 校验副本独立性 → `equals` → `createStrokedShape`。

## 3. 开源代码实际用法调研（来源标注）
- JDK 源码 `src/java.desktop/share/classes/java/awt/BasicStroke.java`：dash 数组存储为 `clone()`；`getDashArray` 返回 clone；校验（width<0、cap/join 非法、miterlimit<1 且 MITER、负 dash、全零 dash、负 dash_phase）抛 `IllegalArgumentException`；`equals` 比较 width/join/cap/miterlimit/dash/dash_phase。
- JDK 自带测试（真实用法）：`test/jdk/java/awt/BasicStroke/DashZeroWidth.java`、`test/jdk/java/awt/BasicStroke/DashOffset.java`（`new BasicStroke` + dash）。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 常量数值。
- 各构造器默认值（cap/join/miter/dash）。
- `getDashArray` 值 + 返回副本（改副本不影响 stroke）。
- 校验异常（负宽/坏 cap/坏 join/miter<1/负 dash/全零 dash/负 phase/空 dash）。
- `equals/hashCode`（含 dash 差异）。
- `createStrokedShape`：矩形描边边界扩宽、直线描边非空。
断言 58 项，字段精确断言（headless-safe）。
