# java.lang.annotation.AnnotationFormatError — 设计方案（JDK 27）

## 1. API 功能
Error：注解 classfile 格式非法、反射读取期间抛出。
- 3 个 public 构造器：`(String)`、`(String, Throwable)`、`(Throwable)`
- `serialVersionUID = -4256701562333669892L`

## 2. Java 用户如何使用
```java
throw new AnnotationFormatError("malformed @Foo", ioe);
catch (AnnotationFormatError e) { e.getCause(); }
```

## 3. 开源代码实际用法调研
- JDK `sun/reflect/annotation/AnnotationParser` 解析失败路径。
- JDK `test/jdk/java/lang/annotation/ClassFileGenerator` 驱动坏 classfile。
- ASM/Javassist 回读注解需处理。

## 4. 测试用例设计 + 自动化实现
白盒：Error 子类、3 个 public 构造器。
黑盒：`(String)` message/无 cause；`(String,Throwable)` message+cause 身份；`(Throwable)` message=cause.toString()；null message/null cause 合法；Error/Throwable 捕获；suppression/stackTrace/toString。
序列化：round-trip 保留 message+cause；serialVersionUID 稳定。

## 易错规范细节（JDK 27）
- `(Throwable) e` 的 message 为 `(e == null ? null : e.toString())`。
- 无无参构造器。

## 契约深化（2026-11）
### API 功能
- 构造器矩阵 javap 锁定：恰 3 个声明构造器，无无参构造，与 JDK-8 完全一致（版本差异：无）。
- Error 语义：fillInStackTrace 返回 this、默认零 suppressed、两级 cause 链身份保持、(Throwable) 形态 message==cause.toString()。

### 使用方式
反射精确签名断言 + 构造行为断言。

### 参考来源
javap（jdk-27.0.1）；Throwable 通用契约。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testConstructorMatrix` / `testErrorSemantics`，与 JDK-8 侧同构。
- 易错：Error 与 Exception 不相交须用 Class.isAssignableFrom 断言；真触发路径（畸形注解属性）需字节码crafting，记跳过。
