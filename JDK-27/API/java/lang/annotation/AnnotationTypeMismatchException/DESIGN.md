# java.lang.annotation.AnnotationTypeMismatchException — 设计方案

## 1. API 功能
RuntimeException：注解元素在类被编译后类型发生变化（重编译注解类型但使用方未重编译）时，反射读取该元素抛出：
- 构造：`(Method element, String foundType)`
- 查询：`element()`（发生类型不匹配的注解元素 Method）、`foundType()`（classfile 中实际发现的类型描述符）

## 2. Java 用户如何使用
```java
catch (AnnotationTypeMismatchException e) {
    Method m = e.element();      // 哪个成员变了
    String t = e.foundType();    // 旧类型描述符，如 "I"
}
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/sun/reflect/annotation/AnnotationType.java`：`matchToMethod` 匹配失败时抛出该异常，携带 foundType 描述符。
- JDK `test/jdk/java/lang/annotation/AnnotationTypeMismatchException/`（编译新版注解+旧版使用类）验证真实触发路径。
- Spring `AnnotationUtils` 对注解属性读取会透传该异常给调用方处理版本漂移。

## 4. 测试用例设计 + 自动化实现
白盒：RuntimeException 子类、`element()`/`foundType()` 返回类型、public 构造器签名。
黑盒（真实触发需双版本类重编译，超出单文件确定性范围，故按规范直接构造断言读取语义）：
- `element()` 返回传入的同一 Method 实例、`foundType()` 精确回读；
- RuntimeException 家族可捕获；两个实例互异；
- foundType 为描述符形态；suppression/stackTrace 机制可用。
对应 `AnnotationTypeMismatchExceptionTest.java` 的 `testBlackBox()`。
