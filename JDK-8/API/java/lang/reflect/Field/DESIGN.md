# java.lang.reflect.Field 测试设计案（JDK-8）

> 覆盖目标：完整契约。元数据、8 种基本类型 get/set 往返、静态 vs 实例 null 语义、错对象类型→IAE、
> 私有访问（未 setAccessible→IAE / setAccessible(true) 后成功）、错基本类型访问器→IAE、final 字段
> set→IAE、equals/hashCode/toString/toGenericString、isEnumConstant/isSynthetic（含内部类 this$0）、
> 注解矩阵、getFields/getDeclaredFields 继承语义与 NSEE/NPE 路径。

## 1. API 功能

- 元数据：getName/getType/getGenericType/getModifiers/getDeclaringClass。
- 读写：`get`/`set` + 8 对基本类型 get/set；`get(Object)` 装箱返回。
- 结构：isEnumConstant/isSynthetic/toGenericString；注解 get*Annotations/getAnnotatedType。
- 继承自 AccessibleObject：setAccessible。

## 2. 用户如何使用

- `C.class.getField(name)` 取 public 字段（含继承），`getDeclaredField` 取声明字段。
- `f.setAccessible(true)` 后读写私有字段；基本类型用 `getInt/setInt` 避免装箱。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/lang/reflect/Field.html）。
- OpenJDK 8 `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/Field.java`。
- JCK runtime-8b `tests/api/java_lang/reflect/Field/`。

## 4. 测试用例设计（映射到 FieldTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 元数据/修饰符/泛型类型 | testMetadata |
| P0 | 8 基本类型 get/set 往返 + 装箱 | testPrimitiveRoundTrip |
| P0 | 静态 vs 实例 null 语义 | testStaticVsInstanceNull |
| P0 | 错对象类型→IAE | testWrongObjectType |
| P0 | 私有字段访问控制 | testAccessibility |
| P0 | 错基本类型访问器→IAE | testWrongPrimitiveAccessor |
| P0 | final 字段 set→IAE | testFinalFieldSet |
| P0 | equals/hashCode/toString/toGenericString | testEqualsHashCodeToString |
| P1 | isEnumConstant/isSynthetic（this$0）| testEnumConstantAndSynthetic |
| P1 | 注解矩阵 | testAnnotations |
| P1 | getFields/getDeclaredFields 继承语义 | testFieldDiscovery |

## 易错规范细节（已锁入断言）

- 实例字段 `get(null)`/`set(null,..)` → NPE；静态字段 `get(null)` 合法。
- `get` 对 int 字段返回装箱 Integer；错基本类型 getter（如 getBoolean on int）→ IAE。
- `set` int 字段传 Long/String/null → IAE（拆箱失败）。
- `getField("priv")` → NoSuchFieldException；`getDeclaredField("priv")` 找到私有。
- `getField(null)`/`getDeclaredField(null)` → NPE。

## 深化批次（2026-11-05）

新增方法（只追加，旧断言全保留）：testTypedAccessorRoundTrip / testMismatchExactMessages /
testNullReceiverSemantics / testFinalFieldMutation / testAccessibleRestore /
testGenericFieldModel / testEnumConstantBits / testToStringExact；新增 checkThrowsMsg
（精确异常类 + getMessage() 包含实测片段）。

### 覆盖增量

- typed 访问器全矩阵：int/long/double/boolean/byte/char/short/float/String/Object 字段
  set→get 精确值；JLS 5.1.2 全部拓宽路径（byte→short/int/long/float/double、
  short→int/long/float/double、char→int/long/float/double、int→long/float/double、
  long→float/double、float→double）读侧与写侧（setXxx + set 装箱拓宽）逐一断言；
  byte→char 不属拓宽（装箱 set 也 IAE）；反射写后普通读可见（d.intF 等）。
- final 语义：构造器初始化的实例 final（非编译期常量）setAccessible(true) 后可 set 且普通读
  可见；static final（常量/非常量）setAccessible(true) 也不能写 → IllegalAccessException
  "Can not set static final"。
- setAccessible(false) 复原：私有字段再访问 → IllegalAccessException；public 字段不受影响。
- 泛型模型：`List<String>` 字段 getGenericType→ParameterizedType（raw/actual/owner/typeName）；
  `T` 字段→TypeVariable("T")；toString 擦除 vs toGenericString 保留。
- enum：常量位 0x4019（PUBLIC|STATIC|FINAL|ACC_ENUM 0x4000）、isEnumConstant true /
  isSynthetic false、toString 全等；合成 $VALUES 位 0x101a（…|ACC_SYNTHETIC 0x1000）。
- toString/toGenericString 对 public/private/volatile/transient/static/static final
  字段全等（用 Class.getName() 拼接，稳定）。

### 负例精确异常表（JDK-8 实测）

| 操作 | 异常 | 消息片段（实测） |
|---|---|---|
| getInt(String字段) | IllegalArgumentException | `Attempt to get java.lang.String field` |
| getDouble(boolean字段)/getChar(long字段)/getBoolean(double字段) | IllegalArgumentException | `Attempt to get <type> field` |
| set(int字段,"x") / setLong(int字段) / setDouble(int字段) | IllegalArgumentException | `Can not set int field` |
| setInt(byte字段,300)/setShort(byte字段) | IllegalArgumentException | `Can not set byte field` |
| setInt(char字段,65)/set(char字段,Byte/Integer) | IllegalArgumentException | `Can not set char field` |
| set(String字段,Integer) | IllegalArgumentException | `Can not set java.lang.String field` |
| get/getInt/set 错接收者类型（含 null 接收者之外的错对象） | IllegalArgumentException | `Can not set <type> field`（JDK-8 get/set 同用 Can not set…to） |
| 实例字段 get(null)/typed(null)/set(null,..) | NullPointerException | 消息为 null（无 detail） |
| static final set/setInt/setDouble | IllegalAccessException | `Can not set static final` |
| setAccessible(false) 后访问私有 | IllegalAccessException | `can not access a member` |

### 版本差异（对照 JDK-27）

- 错接收者 get 消息：8 用 `Can not set … to <cls>`（get/set 同 wording），27 用 `Can not get … on <cls>`。
- 实例字段 null 接收者 NPE：8 无消息；27 带 helpful message（`because "obj" is null`）。
- 访问拒绝消息：8 `Class X can not access a member … with modifiers "private"`；27 `cannot access a member … with private access`。
- 27 侧 nestmates：嵌套类私有成员 setAccessible(false) 后仍可访问（复原负例须用非 nestmate 顶层类）。
- 27 对实例 final 字段反射写打 stderr 警告（未来将禁用）但 27.0.1 仍允许；8 无警告。
- API：27 增 accessFlags()；其余一致（javap -public 双侧核对）。
