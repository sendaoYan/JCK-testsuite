# java.lang.constant.DirectMethodHandleDesc — 设计方案

## 1. API 功能
直接方法句柄描述（interface，extends MethodHandleDesc）：`kind()`、`refKind()`、`isOwnerInterface()`、`owner()`、`methodName()`、`lookupDescriptor()`。

## 2. Java 用户如何使用
```java
DirectMethodHandleDesc d = MethodHandleDesc.ofMethod(Kind.VIRTUAL,
        ClassDesc.of("java.lang.String"), "length", MethodTypeDesc.of(CD_int));
MethodHandle mh = d.resolveConstantDesc(MethodHandles.lookup());
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/DirectMethodHandleDesc.java` + `DirectMethodHandleDescImpl`。
- Enum.EnumDesc/DynamicConstantDesc 内部以 BSM_*（本类型）驱动解析。
- JDK `test/jdk/java/lang/constant/DirectMethodHandleDescTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口、父接口、6 方法及返回类型。
黑盒：VIRTUAL String.length 全访问器 + refKind 5 + invocationType 插入接收者 + resolve 调用=5；INTERFACE_VIRTUAL List.size（isOwnerInterface、refKind 9）；CONSTRUCTOR `<init>` lookup 返回 V 而 invocation 返回类；STATIC_GETTER Integer.MIN_VALUE refKind 2 且 resolve 读值；GETTER refKind 1；按值 equals/hashCode；缺方法 resolve 抛 ReflectiveOperationException。
对应 `DirectMethodHandleDescTest.java` 的 `testBlackBox()`。

## 5. 契约深化（JDK-27 实测锁定）
- **Kind 全枚举矩阵**（11 个）：STATIC(6,false)/INTERFACE_STATIC(6,true)/VIRTUAL(5,false)/INTERFACE_VIRTUAL(9,true)/SPECIAL(7,false)/INTERFACE_SPECIAL(7,true)/CONSTRUCTOR(8,false)/GETTER(1,false)/SETTER(3,false)/STATIC_GETTER(2,false)/STATIC_SETTER(4,false)；公共字段 `refKind`/`isInterface` 与访问器一致。
- **Kind.valueOf(int)**：1..9 精确映射（1=GETTER,2=STATIC_GETTER,3=SETTER,4=STATIC_SETTER,5=VIRTUAL,6=STATIC,7=SPECIAL,8=CONSTRUCTOR,9=INTERFACE_VIRTUAL）；0/10/-1 → IAE；valueOf(6,true)→INTERFACE_STATIC。
- **字段访问器真闭环**：SETTER(ofField) resolve 后写实例字段、GETTER 读回 777；字段 kind 的 lookupDescriptor 是字段类型描述符（"I"），invocationType 为 (Owner)I / (Owner,I)V。
- **kind 错配**：VIRTUAL 描述 static 方法（String.valueOf(I)）resolve → **IllegalAccessException**（kind 参与 resolve 语义），非 NoSuchMethodError。
- null 工厂矩阵：ofMethod(null kind/null owner)、ofField(null type) 均 NPE。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
