# java.lang.reflect.Field — 测试设计（JDK-27）

## 1. API 功能
`Field` 表示类/接口上的字段（无 public 构造器，经 `Class.getField/getDeclaredField` 获得）：
- 元数据：`getName/getDeclaringClass/getModifiers/getType/getGenericType/isSynthetic/isEnumConstant/accessFlags`。
- 对象访问：`get/set` 与按类型族 `getBoolean/getByte/getChar/getShort/getInt/getLong/getFloat/getDouble` 及对应 `setXxx`，异常 `IllegalAccessException/IllegalArgumentException`。
- 泛型：`getGenericType/getAnnotatedType`。
- 注解：`getAnnotation/getAnnotations/getAnnotationsByType/getDeclaredAnnotations`。
- 相等与展示：`equals/hashCode/toString/toGenericString`；访问控制 `setAccessible/trySetAccessible/canAccess`。

## 2. Java 用户如何使用
`getField("名")`（public）或 `getDeclaredField`（含私有）→（私有 `setAccessible(true)`）→ `get(obj)/set(obj, val)`（静态字段 obj=null）→ 读回验证。典型链路：
- ORM/序列化（Hibernate、Jackson 字段模式）：`getDeclaredFields` 遍历 → `setAccessible` → `get` 取值装配 JSON/SQL。
- 配置/依赖注入按 `getType()` 判断转换；泛型字段经 `getGenericType` 还原 `List<String>` 之类声明。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Field/Set.java`：`set` 系列的类型匹配（含窄化拒绝 → `IllegalArgumentException`）。
- JDK 自测 `Field/NegativeTest.java`：错误 receiver、final 字段写等负路径。
- JDK 自测 `Field/GenericStringTest.java`：`toGenericString` 输出泛型形态。
- Spring `ReflectionUtils.getField/setField/makeAccessible`（spring-core）：私有字段注入链路（对应场景 6）。
- Guava `CommonObjectPropertySubject`/Apache Commons `FieldUtils.readField/writeField`：装箱 `get/set` + 可达性开关（对应场景 1/3/6）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`FieldTest.testBlackBox`，固定实体 `Entity`/顶层类 `FieldSeparate`）：
1. 全类型族 round-trip：int（装箱+`getLong/getDouble` 宽化读）、byte/short/char/float/long/boolean/String（12+ 断言）。
2. 窄化拒绝：`setLong` 写 int 字段、`getInt` 读 long 字段 → `IllegalArgumentException`。
3. 静态字段：`get(null)`/`set(null,v)` + 经类名读回一致性。
4. final 实例字段写 → `IllegalAccessException`。
5. 数组字段：`get` 返回活引用、元素改动可见、整体替换。
6. 跨 nest 私有字段：先 `IllegalAccessException`，`setAccessible(true)` 后 get/set 往返。
7. receiver 类型错配 → `IllegalArgumentException`。
8. 泛型字段：`getType()==List.class` 擦除、`getGenericType` 为 `ParameterizedType`（raw/arg/typeName 精确），嵌套 `Map<String,List<Integer>>` typeName 精确，`getAnnotatedType().getType()` 为 PT。
9. `@Deprecated` 字段注解三查询。
10. `equals/hashCode` 值语义 + `toString/toGenericString` 形态。
白盒部分保留原结构断言 + final/Member 接口检查。产出：`FieldTest.java`（57 断言通过）。
