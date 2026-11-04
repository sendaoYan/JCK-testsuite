# javax.management.ObjectInstance 黑盒行为测试设计

## 1. API 功能
- 构造：`(String objectName, String className)`、`(ObjectName, String)`。
- 查询：`getObjectName()`、`getClassName()`；`equals/hashCode/toString`。

## 2. Java 用户如何使用
registerMBean 的返回值，描述注册后的 ObjectName 与实现类名。
```java
ObjectInstance oi = mbs.registerMBean(bean, on);
String className = oi.getClassName();
```

## 3. 开源代码实际用法调研
- `test/jdk/javax/management/ObjectInstance/ObjectInstanceNullTest.java`、`ToStringMethodTest.java`。
- 实现权威：`src/java.management/share/classes/javax/management/ObjectInstance.java`。

## 4. 测试用例设计 + 自动化实现
- 黑盒：getObjectName 返回解析后的 ObjectName、getClassName 返回类名；equals/hashCode 对两者敏感；null objectName 构造抛异常（源码确认）；toString 包含两者。
- 白盒：public 类、implements Serializable、含 getObjectName/getClassName。
