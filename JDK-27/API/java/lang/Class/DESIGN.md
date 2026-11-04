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
