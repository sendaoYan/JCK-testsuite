# javax.crypto.spec.GCMParameterSpec 设计案

## 1. API 功能
GCM 模式参数规范（实现 `java.security.spec.AlgorithmParameterSpec`，since 1.7）：
- 构造：`GCMParameterSpec(int tLen, byte[] src)`；`GCMParameterSpec(int tLen, byte[] src, int offset, int len)`（取 src 子区间作 IV）。
- 查询：`getTLen()`（认证 tag 位长）；`getIV()`（每次调用返回新数组）。
- 类级校验（源码 `init()`）：`tLen < 0`、`src == null`、`len/offset < 0`、`len > src.length-offset` 均 `IllegalArgumentException`。注意：类**不**校验 tLen 是否 ∈ {128,120,112,104,96,64,32}，也不限制 IV 长度（0 也接受）；该限制由 Cipher 实现层执行。

## 2. Java 用户如何使用
```java
GCMParameterSpec spec = new GCMParameterSpec(128, iv12);   // 12B IV + 128-bit tag
Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
c.init(Cipher.ENCRYPT_MODE, key, spec);
c.updateAAD(aad); byte[] ct = c.doFinal(pt);               // ct 含尾接 tag
c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv12));
byte[] pt2 = c.doFinal(ct);                                // tag 校验失败 -> AEADBadTagException
```
典型链路：构造 spec → `Cipher.init(ENCRYPT/DECRYPT)` → `updateAAD` → `doFinal` 往返；同一 key/IV/PT 下 GCM 完全确定（可作 KAT）。

## 3. 开源代码实际用法调研
- JDK 自带测试 `~/git/jdk25u-dev/test/jdk/javax/crypto/spec/GCMParameterSpec/GCMParameterSpecTest.java`（L46-70）：`new GCMParameterSpec(128, bytes)` / `(96, bytes, 4, 4)` 合法、`(0,0)` 空区间合法，负 tLen/null src/越界 offset-len 抛 IAE——本设计异常矩阵直接对齐该文件。
- JDK 源码 `src/java.base/share/classes/javax/crypto/spec/GCMParameterSpec.java`（L120-135 `init()`）：防御拷贝 + `getIV()` 每次 `clone()`。
- Web 调研（DuckDuckGo/通用搜索）：最常见的开源模式是 `cipher.init(ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv))` 配合 `AES/GCM/NoPadding`（AWS Encryption SDK for Java 等加密库标准用法，128-bit tag + 12B IV）。来源：AWS Encryption SDK for Java (github.com/aws/aws-encryption-sdk-java)、Tink/各类 AES-GCM 工具类通用模式。

## 4. 测试用例设计 + 自动化实现
`GCMParameterSpecTest.java`：
- **白盒**：public/非 final 非 abstract、父类 Object、实现 AlgorithmParameterSpec、两个公共构造器签名、getTLen:int/getIV 存在。
- **黑盒**（场景清单）：
  1. 基本构造 getTLen/getIV + offset 构造器（保留原有 3 断言）；
  2. 防御拷贝三连：改外部 IV 后 getter 不变、两次 getIV 非同一引用内容相等、改 getter 返回值不影响内部；
  3. offset 构造器精确子区间内容；零长 IV 接受；tLen=0 类级接受（延迟校验文档化）；
  4. 异常矩阵 7 项（负 tLen、null src ×2、负 offset、负 len、offset+len 越界）→ IllegalArgumentException；
  5. 真实链路：AES/GCM 固定 key/IV/PT → KAT 密文 `afefbd4d...f4447e5`、同参重加密相等、解密往返；
  6. AAD 往返 + 错 AAD / 篡改密文 → AEADBadTagException；
  7. Cipher 层校验：tLen=0、空 IV → InvalidAlgorithmParameterException；1 字节 IV 往返成功。
