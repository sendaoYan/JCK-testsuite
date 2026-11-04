# javax.crypto.NullCipher 黑盒行为测试设计

## 1. API 功能
- `public NullCipher()`：public 构造器（任务书猜测 protected，实测 javap/反射均为 public）。
- 不声明任何新方法，全部行为继承 `Cipher` 并委派给包内 `NullCipherSpi`：恒等加解密（密文==明文的**拷贝**）、blockSize 恒 1、getOutputSize(n)==n、getIV 恒返回 8 字节非 null 数组、getAlgorithm/getProvider/getParameters/getExemptionMechanism 恒 null。
- 无状态：任何 init 都是 no-op，update/doFinal 在未 init 时也可调用（不抛 IllegalStateException）；wrap/unwrap/updateAAD 未被 SPI 覆写 → UnsupportedOperationException。

## 2. Java 用户如何使用
主要用于测试与协议调试：把真实 Cipher 替换为 NullCipher 验证管道/流链路而不引入密码学行为。
```java
Cipher nc = new NullCipher();          // 无需 init 即可用
byte[] out = nc.doFinal(data);         // 内容相等的新数组
new CipherOutputStream(os, nc)         // 写什么落什么
new CipherInputStream(is, nc)          // 读什么是什么
```

## 3. 开源代码实际用法调研
- `~/git/jdk8u-dev/test/jdk/javax/crypto/NullCipher/TestWithoutInit.java` L42–64：未 init 直接 doFinal/update、wrap/unwrap 抛 UnsupportedOperationException（与本测试断言同源，实测一致）。
- `~/git/jdk8u-dev/test/jdk/javax/crypto/SealedObject/TestSealedObjectNull.java`：SealedObject 以 NullCipher 做 sealed/getObject 冒烟。
- `~/git/jdk8u-dev/test/jdk/com/sun/crypto/provider/CICO/PBEFunc/CipherNCFuncTest.java`：NullCipher 接 CICO（CipherInputStream/OutputStream）功能验证。
- 行为权威：`~/git/jdk8u-dev/src/java.base/share/classes/javax/crypto/NullCipherSpi.java`（engineUpdate L78 做 `new byte[inputLen]+arraycopy` 拷贝、engineGetBlockSize L52 返回 1、engineGetIV L60 返回 `new byte[8]`）与 `NullCipher.java` L46（`super(new NullCipherSpi(), null)` → provider/transformation 为 null）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`NullCipherTest.testBlackBox`，保留原有 3 条断言：instanceof Cipher、blockSize==1、init 后 doFinal 恒等）：
- 拷贝语义：doFinal/update 输出与输入内容相等但**不是同一引用**；update(off,len) 取子区间；5 参 update/doFinal 按 outOff 落位、返回长度正确。
- 未 init 可用（javadoc 承诺"never IllegalStateException"）：update/doFinal/getOutputSize 全部直接可调。
- 元数据：getAlgorithm==null、getProvider==null、getParameters==null、getExemptionMechanism==null、getIV 非 null 且 8 字节、getOutputSize(-1)→IllegalArgumentException。
- init 四种模式（ENCRYPT/DECRYPT/WRAP/UNWRAP）+ AlgorithmParameterSpec + SecureRandom 形态全不抛，且输出恒等不变。
- 不支持操作：wrap/unwrap/updateAAD → UnsupportedOperationException（继承 CipherSpi 默认）。
- 流组合：COS 写 5B 原样落盘；CIS 读回 5B 原样（组合链路闭环）。
注：任务书写 getBlockSize()==0，权威源码与实测均为 1（javadoc 亦声明 "blocksize is set to 1 byte"），按 1 断言。
白盒场景（`testWhiteBox`）：public 具体、直接父类 javax.crypto.Cipher（二进制名）、public 无参构造器、未声明任何方法（行为全继承）。
