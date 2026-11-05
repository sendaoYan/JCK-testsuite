# java.lang.annotation.RetentionPolicy — 设计方案（JDK 8）

## 1. API 功能
枚举：`@Retention` 的取值域。
- 3 个常量：SOURCE（编译丢弃）、CLASS（保留到 classfile，反射不可见，默认）、RUNTIME（反射可见）
- 枚举机制：`values()`/`valueOf(String)`

## 2. Java 用户如何使用
```java
@Retention(RetentionPolicy.RUNTIME) @interface A {}
switch (policy) { case SOURCE: ... case CLASS: ... case RUNTIME: ... }
```

## 3. 开源代码实际用法调研
- JDK `RetentionPolicy.java`：CLASS 为无 @Retention 时的默认。
- Lombok 用 SOURCE/CLASS；Spring/Jakarta 用 RUNTIME。

## 4. 测试用例设计 + 自动化实现
- 结构：枚举、父类 Enum、3 常量、values/valueOf 存在。
- `values()` 恰 3 个且顺序 SOURCE→CLASS→RUNTIME；防御性拷贝。
- 三常量 valueOf 往返；大小写敏感抛 IAE；每个常量 ordinal == 下标（0/1/2）。
- compareTo/name/toString；switch 消费范式；== 身份。

## 易错规范细节（JDK 8）
- 3 个常量在 JDK 8 与 JDK 27 完全一致，可双侧镜像断言。

## 契约深化（2026-11）
### API 功能
- 负例精确消息锁定（双侧逐字一致）：valueOf("BOGUS") → "No enum constant java.lang.annotation.RetentionPolicy.BOGUS"；空串同构；valueOf(null) → NPE。
- 作为注解成员值的消费闭环：@Retention(RetentionPolicy.RUNTIME) 自反可见（只有 RUNTIME 可反射）。
- 枚举单例：values() 与 getEnumConstants() 共享实例；getDeclaringClass；compareTo 精确差值（RUNTIME-CLASS==1、SOURCE-RUNTIME==-2）；三名字逗号串全等 "SOURCE,CLASS,RUNTIME"。

### 使用方式
负例 + 单例身份 + 差值断言。

### 参考来源
java.lang.Enum.valueOf；@Retention javadoc。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testNegativeExactAndIdentity`。
- 易错：恰 3 值且声明序 SOURCE(0)/CLASS(1)/RUNTIME(2)；compareTo 是 ordinal 差不是符号。
