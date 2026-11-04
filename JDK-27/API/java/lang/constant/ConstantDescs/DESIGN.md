# java.lang.constant.ConstantDescs — 设计方案

## 1. API 功能
预定义常量库（final、无 public 构造器）：DEFAULT_NAME/INIT_NAME/CLASS_INIT_NAME；CD_Object…CD_Void 引用类描述；CD_int…CD_void 基本类型；MTD_void；BSM_* 九个引导方法（ConstantBootstraps 属主）；NULL/TRUE/FALSE；`ofCallsiteBootstrap/ofConstantBootstrap` 工厂。

## 2. Java 用户如何使用
```java
DynamicConstantDesc<Boolean> t = ConstantDescs.TRUE;
MethodHandleDesc bsm = ConstantDescs.ofConstantBootstrap(owner, "bs", CD_Integer, CD_String);
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/ConstantDescs.java`。
- Enum.constDescriptor()/记录 ObjectMethods 均引用 BSM_ENUM_CONSTANT、CD_Enum 等。
- JDK `test/jdk/java/lang/constant/ConstantDescsTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：final、无 public 构造器、公共常量数量、两个静态工厂。
黑盒：字符串常量精确值（"_"、"<init>"、"<clinit>"）；CD_* descriptorString 精确断言（含 CD_MethodHandles_Lookup 嵌套名）；CD_String/CD_Object resolve；基本类型 I/J/F/D/V；MTD_void；9 个 BSM 的 owner/methodName/kind/refKind/lookup 前缀；BSM_ENUM_CONSTANT 完整签名；ofCallsiteBootstrap/ofConstantBootstrap 的 (Lookup,String,MethodType/Class,extras)ret 形状；NULL/TRUE/FALSE resolve。
对应 `ConstantDescsTest.java` 的 `testBlackBox()`。
