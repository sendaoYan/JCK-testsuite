# java.lang.annotation.AnnotationFormatError — 设计方案

## 1. API 功能
Error：注解在 classfile 中格式非法时（反射读取注解期间）抛出：
- 3 个 public 构造器：`(String)`、`(String, Throwable)`、`(Throwable)`

## 2. Java 用户如何使用
通常不主动构造，由核心反射在解析坏 classfile 时抛出；测试/工具中模拟：
```java
throw new AnnotationFormatError("malformed @Foo", ioe);
catch (AnnotationFormatError e) { e.getCause(); }
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/sun/reflect/annotation/AnnotationParser.java`：解析失败路径抛出该错误（如非法 proxy 类名、坏枚举常量）。
- JDK `test/jdk/java/lang/annotation/ClassFileGenerator.java` 生成的坏注解 classfile 驱动 `AnnotationFormatError`（配套 badclasses 目录）。
- ASM/Javassist 类字节码工具的“回读注解”路径需处理此错误。

## 4. 测试用例设计 + 自动化实现
白盒：Error 子类、3 个 public 构造器。
黑盒（主动构造覆盖三构造器行为，真实触发需坏 classfile 属编译器测试域）：
- `(String)` message 精确、无 cause；
- `(String,Throwable)` message+cause 身份及 cause message；
- `(Throwable)` message 取 `cause.toString()`；
- 可作为 Error/Throwable 捕获；stackTrace/suppression/toString 机制可用。
对应 `AnnotationFormatErrorTest.java` 的 `testBlackBox()`。
