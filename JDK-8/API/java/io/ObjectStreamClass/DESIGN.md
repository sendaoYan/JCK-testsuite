# java.io.ObjectStreamClass 测试方案 (DESIGN)

## 1. API 功能（按方法分组）
- 静态查找：`lookup(Class)`（非 Serializable 返回 null）。
- 描述查询：`getName()`、`forClass()`、`getSerialVersionUID()`、`getFields()`、`getField(String)`。
- 常量：`NO_FIELDS`。
- `toString()`：`ClassName: static final long serialVersionUID = N;` 形态。
- 注意：`lookupAny(Class)` 为 JDK 9+，本 JDK-8 用例不含。

## 2. Java 用户如何使用
- `ObjectStreamClass.lookup(MyClass.class).getSerialVersionUID()` 校验版本兼容（序列化升级的守门代码）。
- `lookup(cl).getFields()` 枚举将落盘的字段（transient/static 会被排除；基本类型在前按声明序、引用类型在后）。
- 自定义 `readClassDescriptor()` 中比对远端与本地描述符（防 SUID 漂移）。

## 3. 开源代码实际用法调研（真实片段，来源标注）
- `if (ObjectStreamClass.lookup(cl).getSerialVersionUID() != suid) { throw new Error(...); }` — `jdk8u-dev/jdk/test/java/io/Serializable/concurrentClassDescLookup/ConcurrentClassDescLookup.java:70`。
- `ObjectStreamClass desc = ObjectStreamClass.lookup(Dummy50.class);` — `jdk8u-dev/jdk/test/java/rmi/reliability/benchmark/bench/serial/ClassDesc.java:104`。
- 反序列化前校验 `readClassDescriptor` 与本地 `ObjectStreamClass.lookup(...)` 的 UID（GitHub 上常见的反序列化加固模式）。

## 4. 测试用例设计 + 自动化实现（ObjectStreamClassTest.java，纯黑盒）
黑盒场景:
1. lookup 非空/空三例（Serializable 类 / 非 Serializable 类 / String）；lookup 结果按类缓存（同实例）。
2. JDK 稳定类的 SUID 精确断言（String=-6849794470754667710、Long=4290774380558885855、Integer=1360826667806852920、ArrayList=8683452581122892189）+ 用户声明 SUID=4242L。
3. name/forClass 精确。
4. 字段表：排除 transient/static；基本类型在前按声明序（I/J/D/Z），引用在后（[、L）；getField 精确/未知；String 的 NO_FIELDS。
5. toString 包含类名与 SUID。
6. 描述符与真实流一致：按该描述符序列化往返值相等。
7. 枚举（0 字段）描述符。
- 健壮性: SUID 为规范固定值；字段顺序是序列化规范规定（primitives 先、按声明序），跨平台稳定。
