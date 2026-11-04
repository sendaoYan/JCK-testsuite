# javax.crypto.spec.RC5ParameterSpec 设计案

## 1. API 功能
RC5 算法参数（version、rounds、wordSize + 可选 IV），`public class implements java.security.spec.AlgorithmParameterSpec`：
- 构造：`RC5ParameterSpec(int version, int rounds, int wordSize)`（无 IV、无取值校验，rounds=0/wordSize 任意值原样存储）；`(v, r, ws, byte[] iv)`；`(v, r, ws, byte[] iv, int offset)`。IV null → IllegalArgumentException("IV missing")；`iv.length - offset < 2*(wordSize/8)` → IllegalArgumentException("IV too short")；offset<0 → ArrayIndexOutOfBoundsException；IV 防御拷贝，长度 = 2*(wordSize/8)（ws=32→8 字节，ws=16→4 字节）。
- 查询：`getVersion()`/`getRounds()`/`getWordSize()`/`getIV()`（副本或 null）。
- equals/hashCode：按 version+rounds+wordSize+IV 重写，`hashCode = Arrays.hashCode(iv) + version + rounds + wordSize`。

## 2. Java 用户如何使用
`new RC5ParameterSpec(0, 16, 32, iv)` → 作为 AlgorithmParameterSpec 交给 Cipher/AlgorithmParameters。注意：Temurin 27 SunJCE 未注册任何 RC5 Cipher（实测 `RC5/CBC/PKCS5Padding` → NoSuchAlgorithmException），故本类黑盒以数据行为为主，不做 RC5 加解密链路。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（关键行为均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/RC5ParameterSpec.java`：构造校验（`blockSize = (wordSize / 8) * 2`）与哈希公式；三参构造无任何校验（实测 rounds=0 原样存储）。
- `test/jdk/java/security/spec/IsAssignableFromOrder.java`：反射列出含 RC5ParameterSpec 在内的 spec 类。
- 本机实测：`Cipher.getInstance("RC5/CBC/PKCS5Padding")` → NoSuchAlgorithmException（SunJCE 无 RC5），DESIGN 据此确定黑盒范围。

## 4. 测试用例设计 + 自动化实现
`RC5ParameterSpecTest.java` 白盒（9 check）：public/非 final/extends Object/implements AlgorithmParameterSpec、6 个方法名、3 个公共构造器、equals/hashCode 覆写。黑盒（26 check）场景：
1. 保留旧断言：v0/r16/ws32 + IV8 的 4 个 getter 与 equals。
2. 无 IV 构造：getter 精确、IV null；wordSize 16 → 4 字节 IV（块长随字长缩放）；offset 构造器窗口 + 与普通构造 equals。
3. 无校验边界：rounds=0 原样存储（version 1/wordSize 16 组合）。
4. 防御拷贝：构造后改源 IV、getIV 改返回值、两次 getIV 不同引用。
5. equals/hashCode 契约：null-IV 对等、rounds/version/wordSize/IV 任一不同即不等、hashCode 一致、自反/null/异类型 false、instanceof。
6. 异常：null IV IAE、7 字节（ws32 需 8）IAE、offset 版不足 IAE、负 offset AIOOBE。
