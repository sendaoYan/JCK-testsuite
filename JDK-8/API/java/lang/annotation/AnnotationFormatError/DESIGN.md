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

## 契约深化（2026-11）
### API 功能
- 构造器矩阵 javap 锁定：恰 3 个声明构造器 (String)/(String,Throwable)/(Throwable)，**双侧均无无参构造**（NoSuchMethodException 实证）；参数类型列表逐一精确。
- Error 语义：fillInStackTrace 返回 this、默认无 suppressed、绝不是 Exception、两级 cause 链身份保持、(Throwable) 形态 message==cause.toString()。

### 使用方式
反射 getDeclaredConstructor 精确签名断言 + 构造行为断言。

### 参考来源
javap 双侧 java.lang.annotation.AnnotationFormatError；Throwable 通用契约。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testConstructorMatrix`：3 构造器 + 无 () 构造器负例。
- `testErrorSemantics`：toString 全等 "java.lang.annotation.AnnotationFormatError: x"、Error↔Exception 家族方向、深 cause 链。
- 易错：`e instanceof Exception` 对 Error 子类是**编译错误**（inconvertible types），须用 `Exception.class.isAssignableFrom`。
- 真触发（畸形 RuntimeVisibleAnnotations 属性）需手工造字节码，按批次简报记为跳过，仅锁构造路径。
