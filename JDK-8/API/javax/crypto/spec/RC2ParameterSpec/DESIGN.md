# javax.crypto.spec.RC2ParameterSpec 设计案

## 1. API 功能
RC2 算法参数（有效密钥位数 + 可选 8 字节 IV），`public class implements java.security.spec.AlgorithmParameterSpec`：
- 构造：`RC2ParameterSpec(int effectiveKeyBits)`（无 IV）；`(int, byte[] iv)`；`(int, byte[] iv, int offset)`（取 [offset, offset+8)）。IV null → IllegalArgumentException("IV missing")；长度不足 8 → IllegalArgumentException("IV too short")；offset<0 → ArrayIndexOutOfBoundsException；IV 均防御拷贝。
- 查询：`getEffectiveKeyBits()`；`getIV()` 返回副本或 null（无 IV 构造时）。
- equals/hashCode：按 effectiveKeyBits + IV（null==null）重写，`hashCode = Arrays.hashCode(iv) + effectiveKeyBits`。

## 2. Java 用户如何使用
`new RC2ParameterSpec(128, iv)` → `Cipher.getInstance("RC2/CBC/PKCS5Padding").init(ENCRYPT_MODE, rc2Key, spec)` → doFinal；两 spec 相等即可互换用于 init（实测相等副本解密成功）。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（关键行为均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/RC2ParameterSpec.java`：构造校验与 `Arrays.hashCode(iv) + effectiveKeyBits` 哈希公式。
- `test/jdk/java/security/spec/RC2ParameterSpec/InvalidArrayIndex.java` L37-39：`Utils.runAndCheckException(() -> new RC2ParameterSpec(0, new byte[20], Integer.MIN_VALUE), ArrayIndexOutOfBoundsException.class)` —— 负 offset 精确类型断言（本测试同款）。
- `test/jdk/javax/crypto/spec/RC2ParameterSpec/RC2AlgorithmParameters.java` L67-91：`new RC2ParameterSpec(2, iv)` 经 AlgorithmParameters 编码往返并比较（本测试改用 Cipher 真实链路）。

## 4. 测试用例设计 + 自动化实现
`RC2ParameterSpecTest.java` 白盒（9 check）：public/非 final/extends Object/implements AlgorithmParameterSpec、4 个方法名、3 个公共构造器、equals/hashCode 覆写。黑盒（26 check）场景：
1. 保留旧断言：getEffectiveKeyBits/getIV/无 IV null/equals/hashCode。
2. offset 构造器：24 字节缓冲 offset 8 取窗口、与普通构造 equals。
3. 防御拷贝：构造后改源 IV、getIV 改返回值、两次 getIV 不同引用。
4. equals/hashCode 契约：null-IV 对等/含 IV 不等、bits 不同/IV 不同不等、自反/null/异类型 false、hashCode 一致。
5. 异常：null IV IAE、7 字节 IAE（含 offset 版）、负 offset AIOOBE。
6. 真实链路：SecretKeySpec("RC2") + RC2ParameterSpec(128, iv) 驱动 Cipher("RC2/CBC/PKCS5Padding")：35 字节明文 → 40 字节密文 → 相等 spec 副本解密还原；错误 IV 首块错乱（多块明文确保 padding 有效、结果确定不等）。
