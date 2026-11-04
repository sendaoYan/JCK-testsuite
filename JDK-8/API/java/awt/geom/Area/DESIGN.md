# java.awt.geom.Area 设计案

## 1. API 功能分组
- **构造器**：`()`（空区域）、`(Shape)`（从任意 Shape 构造）。
- **布尔组合（原地）**：`add(Area)`、`subtract(Area)`、`intersect(Area)`、`exclusiveOr(Area)`、`reset()`。
- **形状分类**：`isEmpty()`、`isPolygonal()`、`isRectangular()`、`isSingular()`。
- **几何**：`getBounds2D()/getBounds()`、`contains/intersects`、`getPathIterator(AT/flatness)`。
- **变换/复制**：`transform(AT)`、`createTransformedArea(AT)`、`clone()`、`equals(Area)`。

## 2. Java 用户如何使用
```
Area a = new Area(new Rectangle2D.Double(0,0,10,10));
Area b = new Area(new Rectangle2D.Double(5,0,10,10));
Area u = new Area(a); u.add(b);            // 并集
Area d = new Area(a); d.subtract(b);       // 差集
Area i = new Area(a); i.intersect(b);      // 交集
Area x = new Area(a); x.exclusiveOr(b);    // 对称差
boolean empty = d.isEmpty();
```

## 3. 开源代码实际用法调研（来源标注）
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Area/AreaTransformTest.java`：transform。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Area/EqualsNull.java`：equals(null)。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Area/BoundsCache.java`：getBounds2D 缓存。
- `~/git/jdk25u-dev/test/jdk/java/awt/geom/Area/TightBBox.java`：紧凑包围盒。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/geom/Area.java`：curve 向量 + 布尔运算实现。

## 4. 测试用例设计 + 自动化实现
黑盒（`AreaTest.testBlackBox()`，30–60 断言）：
1. 默认空 Area、Area(Shape) 矩形/椭圆。
2. 分类：isRectangular/isSingular/isPolygonal（矩形 vs 椭圆）。
3. add/subtract/intersect/exclusiveOr 的精确 bounds。
4. 相同区域 subtract/xor 得到 empty。
5. reset、isEmpty。
6. getBounds2D/getBounds（整型）。
7. contains/intersects。
8. transform、createTransformedArea、clone 独立性、equals(Area)、iterator 段数。
9. 空 Area 代数：add/subtract 以空 Area 为单位元（bounds 不变），intersect 以空 Area 为零元（结果 empty）。
10. 两个相离矩形的并：isSingular=false、isRectangular=false、bounds 跨两者、缝隙不含、两块均含。
11. 矩形挖洞：isSingular=false 仍 isPolygonal、洞不含、环体含。
白盒：implements Shape/Cloneable、布尔/分类/变换方法存在。
