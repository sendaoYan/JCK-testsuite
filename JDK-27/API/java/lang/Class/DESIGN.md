# Class — 设计方案

## 1. API 功能
`java.lang.Class<T>` 表示类/接口/数组/基本类型/枚举/注解的运行时元数据：
- 名称：`getName/getSimpleName/getCanonicalName/getTypeName`
- 分类：`isInterface/isArray/isPrimitive/isEnum/isAnnotation/isRecord/isAnonymousClass/isLocalClass/isMemberClass`
- 关系：`getSuperclass/getInterfaces/getComponentType/isAssignableFrom/isInstance/cast/asSubclass`
- 加载：`forName`（3 个重载）、`getClassLoader`
- 反射入口：`getDeclared*`/`get*` 系列

## 2. Java 用户如何使用
- `obj.getClass()` 获取运行时类型；`Class.forName("...")` 按名加载；`int.class`/`String[].class` 用字面量。
- `Class.isAssignableFrom`/`isInstance` 做运行时类型判断；`cast` 做受检强转。
```java
Class<?> c = Class.forName("java.lang.String");
if (Number.class.isAssignableFrom(c)) { Number n = (Number) c.getDeclaredConstructor().newInstance(); }
```

## 3. 开源代码实际用法调研
- Spring `ClassUtils`/`ReflectionUtils` 大量使用 `Class.forName`、`isAssignableFrom`、`getDeclaredMethods`。
- Guava `TypeToken` 依赖 `getGenericSuperclass`/`getTypeParameters`。
- JDK `test/jdk/java/lang/Class` 系列验证 `forName`、`isAnonymousClass`、`getCanonicalName`。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Class")` 验证 final、非接口、父类为 `Object`、无公开构造器。
黑盒：基本类型/数组/字符串的名称与分类；继承与接口关系；`isInstance/isAssignableFrom/cast/asSubclass`；`forName` 三重重载 + 缺失类抛 `ClassNotFoundException`；匿名/局部/成员类分类。
对应 `<Simple>Test.java` 黑盒部分。

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 同 JDK-8 侧全矩阵（原始类型 9 类名称/TYPE、数组描述符、嵌套/匿名/局部命名、cast/isInstance/asSubclass 精确消息、newInstance 负例、getDeclaredMethod 精确签名、getEnumConstants 防御拷贝、forName 初始化计数闭环、资源 CAFEBABE）。
- 27 专属：isRecord（自造 record Point(int x, String tag)：getSuperclass==java.lang.Record、getRecordComponents 名称/类型/declaringRecord、非 record → null、toString "Point[x=1, tag=a]"、值语义 equals/hashCode）；isHidden（lambda 类为 hidden class（JEP 371，JDK-15+ 实现）→ true；普通/匿名/局部/原始类型均 false）；isSealed/getPermittedSubclasses（非 sealed → null）；getPackageName（原始类型按规范报 "java.lang"、default 包为 ""、数组跟随组件）；getModule（java.base named、toString "module java.base"、classpath 类 unnamed）；descriptorString/describeConstable（"Ljava/lang/String;"/"I"/"[I"/"V"）；toGenericString record 形式 "static final record ClassTest$Point"。
- 版本差异实测：Enum implements {Comparable, Serializable}（8）vs {Constable, Comparable, Serializable}（27）；Integer generic interfaces 1 个（8）vs 3 个（27）；**嵌套私有构造 newInstance：8 抛 IllegalAccessException（无 nestmate），27 成功（nestmate）**；default 包 getPackage null（8）vs 非 null 空名（27）。

### 2. 使用方式
同上；record/isHidden 等用 27 专属 API 直接编译。

### 3. 参考来源
Oracle javadoc（Class.getPackageName 原始类型规定、record 组件、hidden classes JEP 371、descriptorString JVMS 4.2）；双侧实测。

### 4. 测试用例设计 + 易错规范细节
- **易错 1**：record 的超类是 java.lang.Record 而非 Object。
- **易错 2**：getRecordComponents 对非 record 返回 null（不是空数组）。
- **易错 3**：lambda 类 getName 含 "Lambda" 且地址不可预测（不锁具体名）；canonical 为 null（hidden class）。
- **易错 4**：getPackageName(int.class)=="java.lang" 是 javadoc 明文规定，不是 bug。
- **易错 5**：构造器 NSME 消息在 27 为 ", "（无空格分隔与 8 相同格式）实测 "ClassTest$CtorArgs.<init>(java.lang.Integer,java.lang.Integer)"。
- 覆盖新增：testPrimitiveNameMatrix/testArrayNameMatrix/testNestedNamesDeep/testToStringFamily/testCastContract/testAsSubclassContract/testNewInstanceContract/testFieldLookup/testMethodLookup/testInterfacesContract/testEnumConstantsContract/testForNameInitialization/testResourcePackageMisc/testSuperclassGeneric/testSyntheticAnnotationFlags/testRecordContract/testHiddenAndSealed/testModuleAndPackageName/testDescriptorStrings。
