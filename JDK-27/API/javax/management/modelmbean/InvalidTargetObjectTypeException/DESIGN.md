# javax.management.modelmbean.InvalidTargetObjectTypeException — 设计案

## 1. API 功能
- 单一异常类，继承 `javax.management.modelmbean.ModelMBeanException`。
- 构造器：无参 + `(String message)`，用于表示目标对象类型不匹配。

## 2. Java 用户如何使用
在设置 ModelMBean 的受管资源类型不合法时抛出或捕获。
```java
try { mb.setManagedResource(obj); }
catch (InvalidTargetObjectTypeException e) { log(e.getMessage()); }
```

## 3. 开源代码实际用法调研
- JDK `src/java.management/share/classes/javax/management/modelmbean/` 内部抛该异常。
- 开源项目按惯例 catch/throw，无复杂行为，测试聚焦构造器与消息传递、继承链。

## 4. 测试用例设计
- 白盒：具体类、非接口、继承 `ModelMBeanException`、无参 + String 构造器。
- 黑盒：String 构造器 message 原样返回；无参构造器 message 为 null；instanceof 继承链校验。
