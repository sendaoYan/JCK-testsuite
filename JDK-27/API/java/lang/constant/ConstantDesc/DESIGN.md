# java.lang.constant.ConstantDesc — 设计方案

## 1. API 功能
根接口：`resolveConstantDesc(MethodHandles.Lookup) -> Object`（可抛 ReflectiveOperationException）。ClassDesc/MethodHandleDesc/MethodTypeDesc/DynamicConstantDesc 均派生。

## 2. Java 用户如何使用
```java
ConstantDesc d = ConstantDescs.NULL;      // 无类型视图
Object v = d.resolveConstantDesc(MethodHandles.lookup()); // null
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/java/lang/constant/ConstantDesc.java`。
- javac 生成 condy 时 JVM 调 bootstrap 的返回值即等价 resolve 结果。
- JDK `test/jdk/java/lang/constant/ConstantDescsTest.java`。

## 4. 测试用例设计 + 自动化实现
白盒：接口、唯一方法、返回 Object、异常声明、三个子接口关系。
黑盒：NULL resolve→null；TRUE/FALSE resolve；Integer 名义形式 resolve；ClassDesc 作 ConstantDesc resolve；ClassNotFound 负例（ReflectiveOperationException 子类）；泛型/函数式用法。
对应 `ConstantDescTest.java` 的 `testBlackBox()`。

## 5. 契约深化（JDK-27 实测锁定）
- 四大家族（ClassDesc/MethodTypeDesc/MethodHandleDesc/EnumDesc）经 ConstantDesc 视图 resolve 均回读等值对象；MethodHandleDesc 视图 resolve 后可直接 invoke。
- 解析失败矩阵精确异常：ClassDesc→ClassNotFoundException、MethodHandleDesc(缺方法)→NoSuchMethodException、MethodTypeDesc(缺参数类)→ClassNotFoundException，全部是声明的 ReflectiveOperationException 子类。
- `ConstantDescs.NULL` 可作 HashSet 键（值语义）且 resolve 为 null。

## 版本差异
JDK-8 无此包（java.lang.constant 是 12+、java.lang.IO 是 25+（JEP 495）、java.lang.foreign 是 19+ 预览 / 22+ 正式）；本目录测试仅 JDK-27，不镜像 JDK-8 侧。
