# javax.crypto.spec.DHGenParameterSpec 设计案

## 1. API 功能
DH 参数生成规格（要生成多大的 p 与私钥指数），`public class implements java.security.spec.AlgorithmParameterSpec`：
- 构造：`DHGenParameterSpec(int primeSize, int exponentSize)`，无任何校验（0/极值原样存储）。
- 查询：`getPrimeSize()` 返回素数 p 的位长；`getExponentSize()` 返回私钥指数位长。

## 2. Java 用户如何使用
`new DHGenParameterSpec(2048, 256)` → `AlgorithmParameterGenerator.getInstance("DH").init(spec)` → `generateParameters()` 得 `AlgorithmParameters` → `getParameterSpec(DHParameterSpec.class)` 读出实际 p/g/l → 交给 `KeyPairGenerator("DiffieHellman").initialize(...)` 生成密钥对。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试（关键行为均已本机 JDK 27.0.1 实测核对）：
- `src/java.base/share/classes/javax/crypto/spec/DHGenParameterSpec.java`：纯 getter 数据类，构造器直接赋值。
- `test/jdk/java/security/KeyAgreement/KeyAgreementTest.java` L89-91：`case "DiffieHellman": specs.add(new DHGenParameterSpec(512, 64));` —— 与本测试真实链路同款（AlgorithmParameterGenerator 驱动）。
- `src/java.base/com/sun/crypto/provider/DHParameterGenerator.java`：engineInit(DHGenParameterSpec) 按 primeSize/exponentSize 生成 DH 参数。

## 4. 测试用例设计 + 自动化实现
`DHGenParameterSpecTest.java` 白盒（7 check）：public/非 final 非 abstract/extends Object/implements AlgorithmParameterSpec、getPrimeSize/getExponentSize 方法名、单一公共构造器 (int,int)。黑盒（10 check）场景：
1. 保留旧断言：2048/256 getter。
2. instanceof AlgorithmParameterSpec。
3. 无校验边界：(0,0) 与 (MIN_VALUE, MAX_VALUE) 原样存储。
4. 同参两实例 getter 相等；1024/128 组合。
5. 真实链路（确定性断言，素数生成值本身随机）：AlgorithmParameterGenerator("DH").init(new DHGenParameterSpec(512,128)) → generateParameters → DHParameterSpec：p.bitLength()==512（primeSize 生效）、getL()==128（exponentSize 生效）、g>0 → 用生成参数初始化两个 KeyPairGenerator("DiffieHellman") → KeyAgreement 双方秘密逐字节相等且 64 字节（512/8）。
