# java.awt.geom.AffineTransform 设计案

## 1. API 功能分组
- **构造/工厂**：`new AffineTransform()`（恒等）、拷贝构造、`new AT(m00,m10,m01,m11,m02,m12)`（float/double 两版）、`new AT(double[6]|float[6])`；静态工厂 `getTranslateInstance/getScaleInstance/getShearInstance/getRotateInstance(θ)/(vecx,vecy)/(θ,px,py)/(vecx,vecy,px,py)/getQuadrantRotateInstance(k)/(k,px,py)`。
- **查询**：`getScaleX/getScaleY/getShearX/getShearY/getTranslateX/getTranslateY`（m00,m11,m01,m10,m02,m12）、`getMatrix(double[6])`（按 {m00,m10,m01,m11,m02,m12} 填充）、`getType()` 位标志（TYPE_IDENTITY/TRANSLATION/UNIFORM_SCALE/GENERAL_SCALE/FLIP/QUADRANT_ROTATION/GENERAL_ROTATION/GENERAL_TRANSFORM）、`getDeterminant()`、`isIdentity()`。
- **操作（原地）**：`translate/scale/shear/rotate(θ)/(θ,px,py)/(vecx,vecy)/(vecx,vecy,px,py)/quadrantRotate(k)/(k,px,py)`（右乘）、`concatenate`（this = Tx·this）、`preConcatenate`（this = this·Tx）、`setTransform(AT|6 doubles)`、`setToIdentity/setToTranslation/setToScale/setToShear/setToRotation(θ)/(θ,px,py)/(vecx,vecy)/(θ,vecx,vecy,px,py→实为 setToRotation(double,double,double,double))/setToQuadrantRotation(k)/(k,px,py)`、`invert()`。
- **点/向量变换**：`transform(Point2D,Point2D)`、`transform(Point2D[],…)`、`transform(float/double[], srcOff, dst, dstOff, numPts)` 四种数组重载、`deltaTransform(Point2D|double[])`（忽略平移）、`inverseTransform(Point2D|double[])`。
- **转换/工具**：`createInverse()`、`createTransformedShape(Shape)`、`clone()`、`equals/hashCode`、`toString()`、`Serializable`。

## 2. Java 用户如何使用
典型链路（视图变换）：`AT tx = AT.getTranslateInstance(cx, cy); tx.rotate(angle); tx.translate(-cx, -cy); shape2 = tx.createTransformedShape(shape);`——绕点旋转。图像管线：`AT.getScaleInstance(sw, sh)` 交给 `AffineTransformOp`。逆变换：`pt2 = at.inverseTransform(pt, null)`；`at.invert()` 原地取逆。合成顺序：`a.concatenate(b)` 表示新变换 = 先 a 后 b（对点先作用 b…实际为矩阵左乘 this·? —— 以断言 (1,1)→(12,2) 与 (22,2) 的实测区分 concatenate/preConcatenate）。数组批量：`at.transform(pts, 0, out, 0, n)`。

## 3. 开源代码实际用法调研（来源标注）
- JDK 自身 `src/java.desktop/share/classes/java/awt/Graphics2D.java` 的类注释给出 `transform()`/`setTransform()`/`shear()`/`concatenate()` 组合用法示例（平移+旋转绘制）。
- `test/jdk/java/awt/geom/AffineTransform/TestInvertMethods.java`：`createInverse()` 与 `invert()` 的对拍用法（JDK 自带测试，直接验证两种求逆 API 行为一致）。
- `test/jdk/java/awt/geom/AffineTransform/TestRotateMethods.java`：`rotate(θ)/(θ,px,py)/(vecx,vecy)` 各重载的等价性验证（round-trip 思路）。
- `test/jdk/java/awt/geom/AffineTransform/EqualsHashcode.java` + `EqualsAndHashCode.java`：equals/hashCode 契约。
- `test/jdk/java/awt/geom/AffineTransform/GetTypeOptimization.java`：getType 位标志在多次 set 后的懒计算正确性（本用例的 getType 断言参考其案例）。
- `src/java.desktop/share/classes/java/awt/image/AffineTransformOp.java`：`createInverse` 用于逆向映射变换像素（JDK 内部真实用法）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AffineTransformTest.testBlackBox()`，全部实测校准值，纯内存确定性）：
1. 恒等阵：isIdentity/getType==IDENTITY/六分量；6 参构造、double[]/float[] 构造、拷贝构造分量与 equals。
2. getMatrix 填充顺序 {m00,m10,m01,m11,m02,m12}；shear 分量语义（getShearX=m01、getShearY=m10）。
3. 工厂族：translate/scale(uniform|general)/shear/rotate(π/2 精确映射 (3,4)→(-4,3))/rotate(θ,px,py)/rotate(vec)/quadrantRotate(k∈{1,2,3})，每厂一个已知点映射断言 + getType 位标志（TRANSLATION=1、UNIFORM_SCALE=2、GENERAL_SCALE=4、FLIP=64、QUADRANT_ROTATION=8、GENERAL_ROTATION=16、复合 translate+scale=3）。
4. setToXxx 重置语义（复用脏对象后回到目标形态）；链式 translate/rotate 复合点映射；quadrantRotate 实例方法。
5. concatenate vs preConcatenate 顺序差异（A=translate(10,0)、B=scale(2,2)：(1,1) 分别 → (12,2)/(22,2)）；setTransform(AT)/setTransform(6 参)。
6. 求逆：createInverse 乘积回恒等、invert() 原地、奇异阵（scale(0,1)）两者均抛 NoninvertibleTransformException；getDeterminant（translate=1、scale(2,3)=6、奇异=0、rotate(π/3)≈1 容差 1e-9）。
7. 变换 API：Point2D(null 自动分配/复用)、Point2D[] 批量、float[]→double[]/double[]→float[]/in-place、inverseTransform(Point2D/double[])（translate(5,7) 下 (1,2)→(-4,-5)）、deltaTransform 忽略平移但应用缩放。
8. createTransformedShape 边界精确；clone 独立；equals/hashCode 契约（同阵等+同 hash、异阵不等、非 AT 类型 false）；toString 精确格式；序列化 round-trip equals。
9. 数值边界：NaN 分量阵 getType==FLIP|GENERAL_SCALE(68) 而非 GENERAL_TRANSFORM；transform 对 NaN 输入传播 NaN；determinant 为 NaN 时 createInverse 不抛异常（返回 NaN 分量阵）；+Inf 平移仍 TYPE_TRANSLATION；JDK-9+ equals 按位比较故两个 NaN 阵相等（JDK-8 用 == 比较为不等——版本差异，两侧各自断言）。
10. 其余向量旋转重载：getRotateInstance(vec,px,py) 保持枢轴、getRotateInstance(0,1) 把 (1,0) 映到 (0,1)、setToRotation(vec) getType==QUADRANT_ROTATION、getRotateInstance(3,4) 行列式=1、getQuadrantRotateInstance(k,px,py) 保持枢轴。
白盒（`testWhiteBox()`）：Class.forName 形态、implements Cloneable/Serializable、公共静态 TYPE_* 常量集、关键方法签名存在。
