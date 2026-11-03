# java.awt.Dimension 黑盒行为测试设计

## 1. API 功能
- 构造：`Dimension()`（0x0）、`Dimension(Dimension)`、`Dimension(int,int)`。
- 公开字段：`width`、`height`（可变值对象）。
- 查询：`getWidth()`/`getHeight()`（Dimension2D 双精度视图）、`getSize()`、`equals(Object)`、`hashCode()`、`toString()`。
- 修改：`setSize(int,int)`、`setSize(Dimension)`、`setSize(double,double)`（向上取整到 int）。

## 2. Java 用户如何使用
最常用的可变尺寸值对象：组件 `getSize()`/`setPreferredSize()` 的载体，布局计算中反复 resize。
```java
Dimension d = new Dimension(3, 4);
d.setSize(10, 20);
Dimension copy = new Dimension(d);
if (d.equals(other)) { ... }
d.setSize(10.7, -3.9);   // -> (11, -3) 向上取整
```
组合链路：`new Dimension` → `setSize` → `getSize` 读回 → `equals` 比较 → `toString` 展示。

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/awt/`（如 `java/awt/Frame/` 系列）大量 `frame.getSize()` 返回值比较、`new Dimension(w,h)` 构造期望值。
- JDK 源码 `src/java.desktop/share/classes/java/awt/Dimension.java`：`setSize(double,double)` 用 `Math.ceil`；`hashCode = (w+h)*(w+h+1)/2 + w`。
- JDK `test/jdk/java/awt/geom/Dimension2D/`：Double 视图 getWidth/getHeight 用法。

## 4. 测试用例设计 + 自动化实现
场景（黑盒 `testBlackBox()`）：
- 三构造器 + 默认 0x0 + 拷贝独立性（mutate copy 不影响原）。
- `setSize(int,int)`/`setSize(Dimension)` 值拷贝、参数不别名。
- `setSize(double,double)` ceil 语义：10.7→11、-3.9→-3（实测 JDK-27 行为）。
- `getWidth/getHeight` 双精度视图；`getSize()` 返回新对象（改它不影响原）。
- `equals`：相等/交换宽高/null/异类/自反 + hashCode 契约。
- `toString` 精确格式 `java.awt.Dimension[width=5,height=6]`。
- 组合链：构造→resize→读回→比较→toString 反映突变。
断言约 28 项，全部纯数学精确断言（headless-safe）。
