# java.lang.invoke.SerializedLambda — 设计方案

## 1. API 功能
lambda 序列化替换形式（public final，实现 Serializable）：公有 10 参构造（capturingClass + 接口类/名/签名 + impl kind/class/name/signature + instantiatedMethodType + capturedArgs）；访问器 `getCapturingClass/getFunctionalInterface*/getImpl*/getImplMethodKind/getInstantiatedMethodType/getCapturedArgCount/getCapturedArg`；`readResolve` 私有。

## 2. Java 用户如何使用
```java
ObjectOutputStream oos = new ObjectOutputStream(bos) { { enableReplaceObject(true); }
    protected Object replaceObject(Object o) { if (o instanceof SerializedLambda s) return s; return o; } };
oos.writeObject((java.io.Serializable & F) s -> s.length());
```

## 3. 开源代码实际用法调研
- JDK `SerializedLambda.java` 与 `test/jdk/java/lang/serialize/`。
- 反序列化校验框架（ObjectInputFilter 教程）以 SerializedLambda 为白名单形态；Jackson/GSON 特判它防泄漏捕获值。

## 4. 测试用例设计 + 自动化实现
白盒：final、Serializable、公有 10 参构造形状（Class,String,String,String,int,String,String,String,String,Object[]）、访问器齐全。
黑盒：捕获静态辅助 lambda（F extends Serializable）得 fiClass="SerializedLambdaTest$F"、fiSig="(Ljava/lang/String;)I"、impl 为 lambda$ 前缀、kind=REF_invokeStatic、0 捕获值；round-trip 反序列化可计算（len("four")*2==8）；捕获局部变量→count=1、arg0=="abc"；String::length→kind=REF_invokeVirtual、implClass="java/lang/String"；公有构造直建替换体逐字段回读；非 Serializable 接口 lambda 不可替换。
对应 `SerializedLambdaTest.java` 的 `testBlackBox()`。