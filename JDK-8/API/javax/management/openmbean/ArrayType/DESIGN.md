# javax.management.openmbean.ArrayType — 测试设计

## 1. API 功能
数组 open 类型：`ArrayType(int dimension, OpenType)`、`ArrayType(SimpleType, boolean primitiveArray)`
（true=基本类型 1 维，false=包装 1 维，等价 getArrayType）、静态 getArrayType(OpenType)（包装元素
升维）、getPrimitiveArrayType(Class)（基本类型数组专用）。getter：getDimension/getElementOpenType/
isPrimitiveArray。

## 2. Java 用户如何使用
getArrayType(SimpleType.STRING) 表 String[]；getPrimitiveArrayType(int[].class) 表 int[]；
new ArrayType(2, SimpleType.INTEGER) 表 Integer[][]；new ArrayType(2, primIntArray) 维度相加(2+1=3)
且保持 primitive（==getPrimitiveArrayType(int[][][].class)）。

## 3. 参考来源
- Oracle javadoc：ArrayType（构造约束、getPrimitiveArrayType 8 类基本数组映射）。
- OpenJDK test/jdk/javax/management/openmbean/ArrayTypeTest.java（思路借鉴，代码自写）。
- 本地实测探针（双侧）。

## 4. 测试用例设计（ArrayTypeTest.java）
- 8 个基本类型数组全表：className [I/[J/[Z/[D/[B/[S/[C/[F、typeName==className、
  elementOpenType==对应 SimpleType（**非 null**，实测）、dim 1、primitive true、desc
  "1-dimension array of <t>"、equals/hashCode。
- toString 双形态精确串（name=,dimension=,elementType=,primitiveArray=）。
- 构造路径等价：(SimpleType,false) == getArrayType；(SimpleType,true) == getPrimitiveArrayType；
  getArrayType(prim) == getPrimitiveArrayType(int[][].class)；new ArrayType(2,prim) == [[[I 形态。
- isValue 矩阵：String[]（含 null 元素）true、Object[] 持 String false、错维 false、Integer[][] 对
  2 维 true/对 1 维 false、null false、int[] 对 primitive true/Integer[] false/int[][] false。
- 序列化 round-trip（包装/基本/2 维/嵌套）equals。
- 负例精确表：dimension 0/-1 → IAE "Value of argument dimension must be greater than 0"；
  (STRING,true) → OpenDataException "Element type is not primitive: java.lang.String"；
  getPrimitiveArrayType(非数组) → IAE "arrayClass must be an array"（含 int.class 原型）；
  getPrimitiveArrayType(String[].class) → IAE "component type of the array must be a primitive type"；
  null 元素类型（构造/getArrayType）→ NPE（仅断言类型，消息双侧不同）。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
