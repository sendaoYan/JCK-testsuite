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
