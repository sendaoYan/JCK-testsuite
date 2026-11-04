# java.lang.reflect.Array — 测试设计（JDK-27）

## 1. API 功能
`Array` 静态工具类，动态创建与访问数组（组件类型运行期才确定）：
- 创建：`newInstance(Class<?>, int)`、`newInstance(Class<?>, int...)` 多维。
- 长度：`getLength(Object)`。
- 取值：`get`（装箱）与 `getBoolean/getByte/getChar/getShort/getInt/getLong/getFloat/getDouble`（宽化读允许、窄化读拒绝）。
- 存值：`set`（可拆箱）与对应 `setXxx` 系列（宽化写允许）。
- 异常：`NullPointerException/IllegalArgumentException/ArrayIndexOutOfBoundsException/NegativeArraySizeException`。

## 2. Java 用户如何使用
组件类型来自配置/反射（`field.getType().getComponentType()`）→ `newInstance` 造数组 → 逐元素 `setXxx/getXxx` 装填/读取 → 用 `getLength` 驱动遍历。泛型算法（对 `Object` 写入未知类型数组）和数组深度拷贝/转换是最常见场景。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Array/ExceedMaxDim.java`：`newInstance` 维度限制与 `IllegalArgumentException`。
- JDK 源码 `~/git/jdk25u-dev/src/java.base/share/classes/java/lang/reflect/Array.java`：native 实现与宽化/窄化规则权威定义。
- Spring `MethodInvoker`/`ReflectionUtils`：变长参数 invoke 时以 `Array.newInstance+set` 装配参数数组。
- Commons Lang `ArrayUtils.toPrimitive/toObject` 变长工具的同构组合（创建→逐元素 set→读回，对应场景 2/4）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ArrayTest.testBlackBox`）：
1. 创建族：int/boolean/double/String、`Integer[].class` 组件得二维、`newInstance(int.class, 2, 3)` 两种重载形态。
2. 全类型 get/set 家族：boolean/byte/short/char/int/long/float/double 精确往返 + byte/short/char/float 宽化写入 int[]/double[] 再读回 + 装箱 get/set。
3. 装箱与宽化读：`set(long[], Integer)`、`getDouble` 读 int 元素、`get` 返回 `Integer`。
4. 对象数组与嵌套数组元素读写。
5. 越界：负下标、index==length、对象数组 99 → `ArrayIndexOutOfBoundsException`。
6. 负长度 → `NegativeArraySizeException`。
7. 非数组 receiver：`getLength/get/set/setInt` → `IllegalArgumentException`。
8. 元素类型错配：String 装入 int[]、null 装入 int[]、`setInt` 于 String[]、`setBoolean` 于 int[]、窄化读 `getInt(long[])` 拒绝、`getLong(int[])` 宽化允许。
9. 零长度数组 + 越界。
白盒保留原有创建/读写断言 + public final 结构。产出：`ArrayTest.java`（45 断言通过）。
