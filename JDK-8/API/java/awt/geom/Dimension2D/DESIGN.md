# java.awt.geom.Dimension2D 设计案

## 1. API 功能分组
- **查询**：`getWidth()` / `getHeight()`（抽象，子类实现存储）。
- **修改**：`setSize(double,double)`（抽象）、`setSize(Dimension2D)`（便捷：把另一维度对象的宽高拷到 this）。
- **工具**：`clone()`（返回独立 Dimension2D 副本）。

## 2. Java 用户如何使用
自定义维度类 `class Size extends Dimension2D { double w,h; ... }`；`size.setSize(320, 200)` 或 `size.setSize(otherSize)` 复用另一尺寸；`Dimension2D copy = (Dimension2D) size.clone()` 得到独立副本。典型组合：`setSize(getPreferredSize())` 链式传递尺寸。

## 3. 开源代码实际用法调研
- JDK 自身 `java.awt.Dimension` 是 `Dimension2D` 的直接子类（`src/java.desktop/share/classes/java/awt/Dimension.java`），重写 `getWidth/getHeight/setSize`——`Dimension2D` 作为尺寸抽象的基类被 AWT 组件尺寸体系广泛使用。
- `test/jdk/java/awt/geom/Dimension2D/` 无专门用例；`Dimension` 的 `setSize(Dimension2D)` 委托给 `setSize(d.getWidth(), d.getHeight())`（源码可见）。
- `java.awt.Component.getMinimumSize/getPreferredSize/getMaximumSize` 的默认实现 `new Dimension(...)` 是 Dimension2D 子类化的真实使用。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`Dimension2DTest.testBlackBox()`，用匿名子类 `Dim` 落地抽象方法）：
1. 构造 + `getWidth/getHeight` 读回。
2. `setSize(double,double)` 更新两维。
3. `setSize(Dimension2D)` 按值拷贝（改源不影响目标），验证拷贝语义而非引用。
4. `clone()` 独立且相等。
5. 负尺寸在 Dimension2D 抽象层合法保存（无归一化）。
6. 跨实现语义：`java.awt.Dimension.equals(任意 Dimension2D)` 为 false（类型检查只认 Dimension）、`Dimension.equals(Dimension)` 按值且 hashCode 一致；`Dimension.setSize(double,double)` 对 (2.6,3.2) 向上取整到 int 字段 (3,4)。
白盒（`testWhiteBox()`）：abstract public class、implements Cloneable、抽象方法 `getWidth/getHeight/setSize(double,double)` 与便捷方法 `setSize(Dimension2D)`/`clone` 存在。
