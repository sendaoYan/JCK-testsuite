# java.lang.reflect.Field 测试设计案（JDK-27）

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

## JDK-27 差异（已锁入断言）

- JDK-11+ nestmates 移除内部类合成 `this$0` 字段：`Outer.Inner.class.getDeclaredFields()` 不含 this$0，
  `getNestHost()` 为顶层类 `FieldTest`；合成字段改为枚举的 `$VALUES`（static final synthetic）。
- `Field.accessFlags()` 返回 `Set<AccessFlag>`（PUBLIC/PRIVATE/STATIC/FINAL 覆盖）。
- 私有访问 IAE 路径改用独立顶层类 `ExtField`（非 nestmate，保证需 setAccessible）。

## 深化批次（2026-11-05）

新增方法（只追加，旧断言全保留）：testTypedAccessorRoundTrip / testMismatchExactMessages /
testNullReceiverSemantics / testFinalFieldMutation / testAccessibleRestore /
testGenericFieldModel / testEnumConstantBits / testToStringExact；新增 checkThrowsMsg
（精确异常类 + getMessage() 包含实测片段）。断言 92→255。

### 覆盖增量

- typed 访问器全矩阵：与 JDK-8 侧同构（JLS 5.1.2 全拓宽路径读写双侧、装箱拓宽、
  byte→char 拒绝、反射写后普通读可见、静态字段 null 接收者全套读写）。
- final 语义：实例 final（构造器初始化，非编译期常量）setAccessible(true) 后可 set 且
  普通读可见；static final 两种形态均拒绝写 → IllegalAccessException
  `Can not set static final`。JDK-27 实测：实例 final 反射写会输出 stderr 警告
  （"Mutating final fields will be blocked in a future release"）但 27.0.1 仍成功。
- setAccessible(false) 复原：用非 nestmate 的 ExtField.hidden（nestmates 下嵌套类私有成员
  复原后仍可访问，负例必须走非 nestmate 类）；public 字段不受 flag 影响。
- 泛型模型 / enum 位 / toString 全等：与 JDK-8 侧同构（0x4019、0x101a、ParameterizedType、
  TypeVariable "T"、toString 擦除 vs toGenericString 保留）。

### 负例精确异常表（JDK-27 实测）

| 操作 | 异常 | 消息片段（实测） |
|---|---|---|
| getInt(String字段) | IllegalArgumentException | `Attempt to get java.lang.String field` |
| getDouble(boolean)/getChar(long)/getBoolean(double) | IllegalArgumentException | `Attempt to get <type> field` |
| set(int字段,"x")/setLong/setDouble(int字段) | IllegalArgumentException | `Can not set int field` |
| setInt(char字段,65)/set(char字段,Byte/Integer) | IllegalArgumentException | `Can not set char field` |
| get/getInt 错接收者 | IllegalArgumentException | `Can not get <type> field`（27 改 wording，get 用 Can not get…on） |
| set 错接收者 / set(String字段,box) | IllegalArgumentException | `Can not set <type> field` |
| 实例字段 null 接收者 get/getLong/getDouble/setBoolean/set | NullPointerException | `because "obj" is null`（helpful NPE） |
| static final set/setInt/setDouble | IllegalAccessException | `Can not set static final` |
| setAccessible(false) 后访问非 nestmate 私有 | IllegalAccessException | `cannot access a member` |

### 版本差异（对照 JDK-8）

- 错接收者 get 消息 wording 差异（Can not get…on vs Can not set…to）。
- helpful NPE（27 有消息 / 8 无消息）。
- 访问拒绝 wording（cannot vs can not…with private access vs with modifiers）。
- nestmates：27 下嵌套类私有成员无需 setAccessible 即可访问；复原负例走 ExtField。
- 实例 final 反射写：27 打 stderr 警告（未来禁用中），8 静默；两侧当前都成功。
- 27 有 accessFlags()（旧批次已覆盖）。
