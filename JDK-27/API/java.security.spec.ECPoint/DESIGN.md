# java.security.spec.ECPoint

## 1. API 功能
表示仿射坐标下的椭圆曲线点，构造参数为仿射 x、y 坐标（`BigInteger`）。静态常量 `POINT_INFINITY` 表示无穷远点，其 x、y 均为 null。重写 `equals/hashCode`（按坐标比较；无穷远点 hashCode=0，且只与自身相等）。

## 2. Java 用户如何使用
用户构造 `new ECPoint(x, y)` 作为基点或公钥点，配合 `ECParameterSpec` 使用；无穷远点直接引用 `ECPoint.POINT_INFINITY`。

## 3. 开源代码实际用法调研
- `test/jdk/java/security/spec/ECCBasic.java:50`：
  ```java
  private static final ECPoint POINT = new ECPoint(ONE, TEN);
  ```
- 实现侧 `src/java.base/share/classes/java/security/spec/ECPoint.java`：`POINT_INFINITY` 通过私有构造把 `x/y` 置 null，`equals` 对 `this == POINT_INFINITY` 特判返回 false。

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、非接口、父类 `Object`，方法 `getAffineX/getAffineY/equals/hashCode` 存在，字段 `POINT_INFINITY` 存在。
- `testBlackBox()`：普通点 `(1,2)` 校验 getter、equals/hashCode 与不等 y；无穷远点校验非 null、`getAffineX()==null && getAffineY()==null`、与自身 equals、hashCode=0、与 `(0,0)` 不等。
