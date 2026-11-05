# javax.management.MBeanAttributeInfo 黑盒行为测试设计

## 1. API 功能
- 构造：`(String name, String type, String description, boolean isReadable, boolean isWritable, boolean isIs)` 及带 Descriptor 的重载。
- 查询：`getType()`、`isReadable()`、`isWritable()`、`isIs()`、继承的 name/description/getDescriptor。

## 2. Java 用户如何使用
标准 MBean 内省生成 MBeanInfo 时，为每个属性生成一个 MBeanAttributeInfo。
```java
MBeanAttributeInfo ai = new MBeanAttributeInfo("Count", "int", "count", true, true, false);
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/MBeanInfo/NotificationInfoTest.java`、`NullInfoArraysTest.java`。
- `test/jdk/javax/management/Introspector/FeatureOrderTest.java`：属性顺序。
- 实现权威：`src/java.management/share/classes/javax/management/MBeanAttributeInfo.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：构造后 getType/isReadable/isWritable/isIs 返回入参；getDescriptor 默认空 descriptor；equals 对关键字段敏感；clone 返回等值对象。
- 白盒：public 类、父类 MBeanFeatureInfo、implements Cloneable、含 getType/isReadable/isWritable/isIs。

## 5. 深化（批 B 追加）
- 6 参构造与 4 参 `Method` 构造：name/type/description 取自 Method；is/is 从方法名前缀推导。
- Method 构造负例：getter 与 setter 类型不一致 → IntrospectionException；getter 带参数 → IntrospectionException；void getter → IntrospectionException；双参 setter → IntrospectionException。
- @DescriptorKey 合并：getter 与 setter 上的注解描述符字段合并进属性描述符；同名 DescriptorKey 字段值冲突（g::s）→ 构造时 IllegalArgumentException。
- hashCode 仅由 name+type 决定（description/descriptor 不参与）；equals 中 name、type、isIs、descriptor 均参与。
- toString：只读/只写/读写/is 分别含 readable/writable/isIs 子串；默认描述符为空；序列化 round-trip。
- 版本差异：无。
