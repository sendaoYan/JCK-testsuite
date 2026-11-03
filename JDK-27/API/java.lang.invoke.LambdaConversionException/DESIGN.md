# java.lang.invoke.LambdaConversionException — 设计方案

## 1. API 功能
lambda 转换失败异常（Exception 子类）：5 个公有构造（无参/message/message+cause/cause/全参含 enableSuppression 与 writableStackTrace）。

## 2. Java 用户如何使用
```java
try { LambdaMetafactory.metafactory(lk, "len", badType, sam, impl, inst); }
catch (LambdaConversionException e) { e.getMessage(); }
```

## 3. 开源代码实际用法调研
- JDK `LambdaMetafactory.java`（altMetafactory 校验路径全抛本类）。
- javac 只在运行时把转换失败呈现给用户：接口非函数式、方法引用签名不符等。

## 4. 测试用例设计 + 自动化实现
白盒：超类 Exception、5 构造存在、serialVersionUID。
黑盒：message-only 保留消息无 cause；cause-only 消息==cause.toString()；全参(false,false) 栈轨迹为空且 suppressed 被丢弃；默认构造可用；suppressed/stackTrace 默认开；真实触发（工厂类型返回非接口）拿到非空 message。
对应 `LambdaConversionExceptionTest.java` 的 `testBlackBox()`。