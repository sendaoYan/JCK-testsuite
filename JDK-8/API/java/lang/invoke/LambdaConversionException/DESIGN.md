# java.lang.invoke.LambdaConversionException — 设计方案

## 1. API 功能
LambdaMetafactory 造桥失败时抛出的受检异常：五个 public 构造 ()/(String)/(String,Throwable)/(Throwable)/(String,Throwable,enableSuppression,writableStackTrace)；(Throwable) 版 message==cause.toString()；可序列化。

## 2. Java 用户如何使用
```java
try {
    LambdaMetafactory.metafactory(lookup, "apply", MethodType.methodType(String.class), ...);
} catch (LambdaConversionException e) { // 非 interface 的 invokedType
    e.getMessage(); // 8: Functional interface java.lang.String is not an interface
                    // 27: java.lang.String is not an interface
}
```

### 参考来源
- Oracle javadoc（8）：https://docs.oracle.com/javase/8/docs/api/java/lang/invoke/LambdaConversionException.html
- Oracle javadoc（27）：https://docs.oracle.com/en/java/javase/27/docs/api/java.lang.invoke/java/lang/invoke/LambdaConversionException.html
- OpenJDK 8u 源码：/data/yansendao/git/jdk8u-dev/jdk/src/share/classes/java/lang/invoke/LambdaConversionException.java
- OpenJDK 27 源码：/data/yansendao/git/jdk/src/java.base/share/classes/java/lang/invoke/LambdaConversionException.java
- JCK runtime-8b：/data/yansendao/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/invoke/LambdaConversionException
（只列路径，不拷内容；JCK 仅作覆盖面对照，代码全部自写。）

## 4. 测试用例设计（含易错规范细节）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 五构造矩阵：() message/cause null；(String)；(String,Throwable) 双精确；(Throwable) message==cause.toString()；全参构造 suppression/writableStackTrace 可观察（addSuppressed 丢弃/保留、getStackTrace 0/非0） | testConstructorChainDeep |
| P0 | 序列化 round-trip message 保留 | testSerializationRoundTrip |
| P0 | initCause 一次后 ISE "Can't overwrite cause"；构造带入的 cause 不可替换 | testInitCauseRules |
| P0 | 真实触发：metafactory invokedType 非接口 → LCE 消息含 "is not an interface"（8/27 公共子串）；对照组正确链接可调用 | testRealTrigger |
| P1 | Throwable 默认面：toString 形态、suppressed 初始空、serialVersionUID 声明、(true,true) 保留栈 | testThrowableDefaults |

易错点：
- metafactory 对"SAM 名不存在"等错误可能在链接期不抛（实测 noSuch 名不抛），触发负例要用"invokedType 非接口"这类确定性错误。
