# java.lang.reflect.Array 测试设计案（JDK-27）

> 覆盖目标：完整契约。newInstance 一维/多维（含负/零/null/void 维度精确异常）、getLength/get/set 全 8 种
> 基本类型 + Object 数组、装箱/拆箱、错类型→IAE、下标→AIOOBE、null 数组→NPE、非数组→IAE、
> 对象数组赋值失败语义、多维分量访问。全部离线，无随机/时钟。

## 1. API 功能

- `newInstance(Class,int)` 与 `newInstance(Class,int...)` 两种工厂。
- `getLength(Object)`、`get(Object,int)`/`set(Object,int,Object)` 及 8 对基本类型 get/set 访问器。
- 全部静态方法，类不可实例化（JDK-8 final，构造器私有）。

## 2. 用户如何使用

- 反射式创建运行时才知道类型/维数的数组：`Array.newInstance(componentType, length)`。
- 反射式读写数组元素：`Array.get(arr,i)` 返回装箱对象，`Array.getInt(arr,i)` 返回原始 int。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/Array.html）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/Array.java`（native 校验逻辑）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/Array/`。

## 4. 测试用例设计（映射到 ArrayTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 一维 newInstance（基本类型/对象/类恒等/负长/零长/null/void）| testNewInstanceOneDim |
| P0 | 多维 newInstance（外层/内层长、负内层维、零维、null 组件/维度）| testNewInstanceMultiDim |
| P0 | getLength（0 长、多维、null→NPE、非数组→IAE）| testGetLength |
| P0 | 8 基本类型 get/set 往返 + 装箱 | testGetSetPrimitives |
| P0 | Object 数组 get/set、异构、错类型、null 元素 | testGetSetObject |
| P0 | 错类型/非数组/null 数组/下标越界异常矩阵 | testExceptions |
| P1 | 多维分量访问（get 行返回 int[]、set 行替换）| testMultiDimAccess |

## 易错规范细节（已锁入断言）

- 负长度 → `NegativeArraySizeException`（不是 IAE）；null 组件类型 → NPE；`void` 组件 → IAE。
- `Array.set(String[], Integer)` → IAE（拆箱后赋值不匹配，非 ArrayStoreException）。
- `Array.set(long[], Integer)` → 拓宽成功；`Array.set(int[], null)` → IAE（拆箱失败，非 NPE）。
- `get`/`getInt` 等对 null 数组 → NPE；对非数组 → IAE；下标越界 → `ArrayIndexOutOfBoundsException`。

## 深化批次（2026-11-05）

新增方法（只追加）：testNewInstanceDimsForm / testBoxedGetSetMatrix / testWideningGetters /
testWideningSetters / testNarrowingRefusalMessages / testNonArrayMessages /
testIndexBoundsExact / testNullArrayNpeMatrix / testMultiDimModel；新增 checkThrowsMsg 与
checkAioobe。断言 84→235；与 JDK-8 侧文件同构（行为逐字节一致，探针双侧实测核对）。

### 覆盖增量

- newInstance 维数组形态（int[]/{2,3}/{2,0}、String[][]）；装箱 get/set 矩阵（8 原生全family）；
  JLS 5.1.2 拓宽格读写双侧 + 装箱 set 拓宽；收窄拒绝消息 `argument type mismatch`；
  非数组消息 `Argument is not an array`（typed family 全覆盖）；
  AIOOBE 精确类 + IndexOutOfBoundsException 子类断言 + 实测无消息（getMessage()==null，
  覆盖 -1/==length/MAX_VALUE/MIN_VALUE/空数组 0 下标）；null 数组 typed 全family NPE；
  多维模型（链式反射 get/getLength、行替换、3 维形状）。

### 负例精确异常表（27 实测 = 8 实测）

| 操作 | 异常 | 消息 |
|---|---|---|
| getInt(long[])/getFloat(double[])/getChar(double[])/getInt(boolean[]) | IllegalArgumentException | `argument type mismatch` |
| setFloat(int[])/setBoolean(int[])/setInt(byte[])/set(int[],Long/"x") | IllegalArgumentException | `argument type mismatch` |
| getLength/get/set/typed 传非数组 | IllegalArgumentException | `Argument is not an array` |
| 下标 -1/越界/MAX/MIN | ArrayIndexOutOfBoundsException | 消息 null（实测） |
| null 数组（全 family） | NullPointerException | 消息 null |

### 版本差异

- 无（javap -public 双侧 API 相同；探针输出逐字节一致）。
