# java.awt.geom.RectangularShape 设计案

## 1. API 功能分组
- **查询**：抽象 `getX/getY/getWidth/getHeight/isEmpty`；派生 `getMinX/getMinY/getMaxX/getMaxY/getCenterX/getCenterY`；`getFrame()`。
- **设框**：抽象 `setFrame(double,double,double,double)`；`setFrame(Point2D,Dimension2D)`、`setFrame(Rectangle2D)`、`setFrameFromDiagonal(...)`、`setFrameFromCenter(...)`。
- **空间关系**：`contains(Point2D)`、`intersects(Rectangle2D)`、`contains(Rectangle2D)`。
- **Shape 协议**：`getBounds()`（java.awt.Rectangle）、`getPathIterator(at, flatness)`；`clone()`。

## 2. Java 用户如何使用
`RectangularShape` 是所有矩形类（Rectangle2D/RoundRectangle2D/Arc2D/Ellipse2D）的抽象基类，用户通过子类实例化，然后用 `getCenterX()/getCenterY()` 取中心、`setFrameFromDiagonal(x1,y1,x2,y2)` 由两角定框、`contains(pt)` 做命中测试、`getBounds()` 取整数外接矩形。

## 3. 开源代码实际用法调研
- `java.awt.geom.Rectangle2D`、`RoundRectangle2D`、`Arc2D`、`Ellipse2D` 均继承 `RectangularShape`（`src/java.desktop/share/classes/java/awt/geom/` 各文件），`setFrame`/`getFrame` 是子类共享的设框协议。
- `java.awt.geom.Area` 内部对 Shape 使用 `getPathIterator` 提取轮廓（PathIterator 协议在 RectangularShape 中提供 flatness 重载）。
- `test/jdk/java/awt/geom/` 各矩形类用例以 `setFrameFromDiagonal`/`setFrameFromCenter` 构造测试图形。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`RectangularShapeTest.testBlackBox()`，用 Rectangle2D.Double 落地抽象方法）：
1. min/max/center/frame 派生查询。
2. setFrame 三重重载；setFrameFromDiagonal 自动归一化角点；setFrameFromCenter 由中心+角点定框。
3. contains/intersects 命中测试；getBounds 整数外接矩形。
4. getPathIterator 迭代 5 段；clone 独立相等。
白盒（`testWhiteBox()`）：abstract public class、implements Shape+Cloneable、抽象 getX/getY/getWidth/getHeight/isEmpty/setFrame 与派生方法签名。
