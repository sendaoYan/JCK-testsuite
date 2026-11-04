# java.lang.constant.DynamicConstantDesc — 设计方案

## 1. API 功能
condy 动态常量描述（abstract implements ConstantDesc）：protected 构造器；`ofNamed/of/ofCanonical`；`constantName/constantType/bootstrapMethod/bootstrapArgs/bootstrapArgsList`；`resolveConstantDesc` 实际调用 bootstrap。

## 2. Java 用户如何使用
```java
DynamicConstantDesc<?> n = DynamicConstantDesc.of(ConstantDescs.BSM_NULL_CONSTANT);
Object v = n.resolveConstantDesc(MethodHandles.lookup()); // null
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/DynamicConstantDesc.java`：resolveConstantDesc 经 MethodHandle.invokeWithArguments 调 bootstrap。
- ConstantDescs.TRUE/FALSE 即本类实例；EnumDesc/VarHandleDesc 派生。
- JDK `test/jdk/java/lang/constant/DynamicConstantDescTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：abstract、protected 构造器、9 方法。
黑盒：ofNamed 全访问器；of(bsm) 默认名 "_"、类型 CD_Object；of(bsm,args) 保留参数与 bootstrapArgsList 视图；nullConstant resolve→null；用户 bootstrap（静态方法）经 resolve 收到 name+type 返回 "abc/String"；TRUE/FALSE resolve；ofCanonical 对 NULL bootstrap 返回共享 NULL；equals/hashCode 含 name/type 参与；toString 内容。
对应 `DynamicConstantDescTest.java` 的 `testBlackBox()`。
