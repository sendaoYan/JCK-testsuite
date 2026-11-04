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
