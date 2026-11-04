# java.lang.annotation.AnnotationFormatError — 设计方案（JDK 8）

## 1. API 功能
`Error`：注解 classfile 格式非法、反射读取注解期间抛出。
- 3 个 public 构造器：`(String)`、`(String, Throwable)`、`(Throwable)`
- `serialVersionUID = -4256701562333669892L`

## 2. Java 用户如何使用
通常不主动构造，由核心反射解析坏 classfile 时抛出；测试/工具中模拟：
```java
throw new AnnotationFormatError("malformed @Foo", ioe);
catch (AnnotationFormatError e) { e.getCause(); }
```

## 3. 开源代码实际用法调研
- JDK `sun/reflect/annotation/AnnotationParser`：解析失败路径抛出该错误。
- JDK `test/jdk/java/lang/annotation/` 的坏 classfile 生成器驱动该错误。
- ASM/Javassist 回读注解需处理此错误。

## 4. 测试用例设计 + 自动化实现
- 结构：Error 子类、3 个 public 构造器（无无参构造器）。
- `(String)` 精确 message、无 cause；`(String,Throwable)` message+cause 身份；`(Throwable)` message=cause.toString()。
- null message/null cause 合法。
- 可作为 Error/Throwable 捕获；suppression/stackTrace/toString。
- 序列化 round-trip：message + cause 存活；serialVersionUID 稳定。

## 易错规范细节（JDK 8）
- `(Throwable) e` 的 message 为 `(e == null ? null : e.toString())`，null 时 message 为 null。
- 无无参构造器（仅 3 个带参构造器）。
