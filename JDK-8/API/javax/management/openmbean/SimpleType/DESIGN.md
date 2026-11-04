# javax.management.openmbean.SimpleType — 测试设计

## 1. API 功能
14 个预定义标量 open 类型单例（私有构造 + static final 常量 + **public readResolve** 保证
序列化后仍返回同一实例）。equals 按 className 判等，hashCode == className.hashCode()。

## 2. Java 用户如何使用
`SimpleType.STRING` 直接作为 OpenType 参数；isValue 做精确运行时类匹配（Integer 对 LONG 为 false）。

## 3. 参考来源
- Oracle javadoc：SimpleType（14 常量表、单例与序列化语义）。
- OpenJDK test/jdk/javax/management/openmbean/IsValueTest.java、EqualsTest.java。
- 本地实测探针（双侧）。

## 4. 测试用例设计（SimpleTypeTest.java）
- 白盒：public final、extends OpenType、单一 private 构造器、14 个 static final 常量、
  public readResolve 声明存在。
- 全表断言（14 常量 × 7 项）：className/typeName==className/description==className/!isArray/
  toString 精确串 `javax.management.openmbean.SimpleType(name=<cn>)`/hashCode==className.hashCode()；
  14 个 hashCode 两两互异。
- 单例语义：常量引用同一；**序列化 round-trip 后 == 同一实例（readResolve）**，round-trip 后
  equals/hashCode 不变。
- isValue 矩阵：14 个正例（含自动装箱）、Integer 拒 Long/Short/null、STRING 拒 StringBuilder、
  OBJECTNAME 拒 String、VOID 拒 null/Void.class。

### 版本差异（javap + 实测，JDK-8 vs JDK-27）
- 公开 API 面双侧完全一致（javap -public 对照）。
- 已锁定的精确异常消息（IAE/OpenDataException/InvalidKeyException/KeyAlreadyExistsException/
  InvalidOpenTypeException 及带消息 NPE）双侧逐字相同。
- 无消息型异常双侧类型一致、消息文本不同：JDK-27 NPE 带 helpful message（如
  "Cannot invoke ... because elementType is null"）、CCE 带 module/loader 说明，JDK-8 无；
  此类只断言异常类型。
- 反射深入度差异：对 java.management 内部位做 setAccessible 时 JDK-9+ 抛
  InaccessibleObjectException（模块未 open），JDK-8 可打开；测试对两种路径均接受。
