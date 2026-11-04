# java.io.OptionalDataException 测试方案 (DESIGN)

## 1. API 功能
- final 类、无公共构造（JDK 内部创建）；公共字段 `int length`、`boolean eof`。

## 2. Java 用户如何使用
- readObject 遇到原始块数据时捕获，读取 length/eof 判断剩余。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/OptionalDataException.java`。

## 4. 测试用例设计 + 自动化实现（OptionalDataExceptionTest.java，纯黑盒）
黑盒场景:
1. readObject 遇原始数据 → eof=false 且 length=4（int）。
2. length 随缓冲原始数据量缩放（long+int=12）。
3. 异常后可继续原始读取恢复数据。
4. 先读原始数据再读对象（对比）。
5. 无 readObject 的类跳过自定义附加数据并正确定位。
- 健壮性: 无反射白盒（JDK 8 无强封装 InaccessibleObjectException）。

## 深化追加（契约级）
追加内容（JDK-8 侧断言数 35，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `OptionalDataException`：双侧同值 `-8011121865681257820L`
### 新增测试方法
1. testReflectiveSurface（无公有构造器；公有 eof/length 字段；final 标志两侧断言不同）
2. testFieldAndThrowableProtocol（触发实例上 initCause/addSuppressed/字段就地赋值）
3. testSerializationContract（触发实例再序列化保留 eof/length）
4. testTriggerMatrix（trigger(int...)：可重复性 ×3、bool+long=9、writeUTF=13、干净 EOF → EOFException 对照）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- 触发实例 message 为 null、toString 无 message 段
- readObject 遇块数据可重复抛（数据不消费）：length 恒 4
- JDK-8 类非 final；JDK-27 类 final（版本差异断言分支）

### JDK-8 vs JDK-27 差异
- JDK-8 类非 final；JDK-27 类 final。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
