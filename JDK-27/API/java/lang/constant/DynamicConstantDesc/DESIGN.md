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

## 5. 契约深化（JDK-27 实测锁定）
- **真 condy 解析闭环（经 ConstantBootstraps）**：
  - `ofNamed(BSM_PRIMITIVE_CLASS,"J",CD_Class)` → long.class；注意 name 必须是**单字符描述符**（"J"），传 "long" 得 BootstrapMethodError(IAE "not primitive: long")。
  - `ofNamed(BSM_GET_STATIC_FINAL,"MAX_VALUE",CD_int,CD_Integer)` → Integer.MAX_VALUE（constantType 用基本 CD_int，declaringClass 作附加参数）。
  - `ofNamed(BSM_EXPLICIT_CAST,"_",CD_int,7)` → 7；`ofNamed(BSM_ENUM_CONSTANT,"SECONDS",CD_TimeUnit)` → TimeUnit.SECONDS。
- **ofCanonical**：NULL 形状返回共享 `ConstantDescs.NULL` 实例；TRUE 形状**不** intern（新实例，但 resolve==Boolean.TRUE）；非规范形状回退 AnonymousDynamicConstantDesc。
- bootstrapArgsList 不可修改（add → UOE）。of/ofNamed(null bsm) → NPE。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
