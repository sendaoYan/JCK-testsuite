# java.io.UTFDataFormatException 测试方案 (DESIGN)

## 1. API 功能
- `(String)` / `()`：modified UTF-8（DataInput.readUTF）编码非法（坏首字节、断续字节、编码长度超 65535）。

## 2. Java 用户如何使用
- readUTF 解码损坏串时捕获；writeUTF 串太长（>65535 编码字节）时同异常。

## 3. 开源代码实际用法调研
- 解码检查 — `jdk25u-dev/src/java.base/share/classes/java/io/DataInputStream.java`（readUTF 的 UTF 字节校验）。
- 长度上限 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectOutputStream.java`（writeUTF 长度检查）与 `DataOutputStream.writeUTF`。

## 4. 测试用例设计 + 自动化实现（UTFDataFormatExceptionTest.java）
- 白盒: extends IOException、两构造。
- 黑盒场景:
  1. 构造器。
  2. 手工字节流矩阵触发：0xFF 首字节 / 0xC3 后接非续字节 / 3 字节序列被声明长度截断。
  3. 声明长度超实体 → EOFException（对照）。
  4. writeUTF 65536+ 双字节字符 → 抛出（长度超限）。
  5. 对照：ASCII/2 字节/3 字节/NUL(C0 80) 正常往返。
  6. 可按 IOException 捕获。
- 健壮性: 手工字节，无随机。

产出：`UTFDataFormatExceptionTest.java`（白盒 4 + 黑盒 12 断言）。

## 深化追加（契约级）
追加内容（JDK-27 侧断言数 52，另一侧见对应 DESIGN）。所有行为断言均为双侧 JDK 实测后写入。
### serialVersionUID 实测值（ObjectStreamClass.lookup，双侧一致）
- `UTFDataFormatException`：双侧同值 `420743449228280612L`
### 新增测试方法
1. testThrowableProtocol（全矩阵）
2. testSerializationContract
3. testDiagnostics（首坏字节位置命名规则：byte 0/1/2 + 尾部截断）
4. testLenientForms（C0 80 必须解码为 NUL；E0 80 80 冗长形式也接受；CESU-8 孤代理/代理对不合并）
5. testWriteBoundary（65535 精确边界 +1 拒绝 + NUL 膨胀）

### 负例与精确异常（操作 → 类型 → 双侧一致 message 子串）
- 首字节 0xFF → 「malformed input around byte 0」
- 游离续字节 0x80@1 → 「malformed input around byte 1」
- C3 41 / E2 28 AC → 「malformed input around byte 2」
- 尾部截断 2/3 字节序列 → 「malformed input: partial character at end」
- 4 字节序列（标准 UTF-8 合法）→ 拒绝，modified-UTF 无此形式
- writeUTF 超 65535 → JDK-8「encoded string too long: N bytes」；JDK-27 同串前缀+截断预览+同尾（版本差异，断言用 endsWith）

### JDK-8 vs JDK-27 差异
- writeUTF 超限 message：JDK-8「encoded string too long: N bytes」；JDK-27 在中间插入截断的字符串预览。断言采用 startsWith("encoded string")+endsWith("too long: N bytes") 兼容双侧。
### 序列化往返清单
- 构造实例（message/toString）往返；- cause 往返（深拷贝）；- suppressed 往返；- 真实触发实例再序列化。
