# java.security.spec.DSAGenParameterSpec

## 1. API 功能
描述 DSA 密钥生成时使用的参数：素数模长度 `primePLen`、子群素数 `q` 长度 `subprimeQLen`，以及可选的种子长度 `seedLen`。两个构造函数：`(int primePLen, int subprimeQLen)` 和 `(int, int, int seedLen)`，后者缺省 seedLen 时等于 subprimeQLen。类为 `final`，实现 `AlgorithmParameterSpec`。

## 2. Java 用户如何使用
用户构造该对象并传给 `AlgorithmParameterGenerator.init(...)` 或 `KeyPairGenerator.initialize(...)`，由 JDK 生成符合规格的 DSA 参数/密钥。

## 3. 开源代码实际用法调研
- `test/jdk/sun/security/provider/DSA/TestAlgParameterGenerator.java:99-102`：
  ```java
  DSAGenParameterSpec[] specSet = {
      new DSAGenParameterSpec(1024, 160),
      new DSAGenParameterSpec(2048, 224),
      new DSAGenParameterSpec(2048, 256)
  };
  ```
- `test/jdk/sun/security/provider/NSASuiteB/TestDSAGenParameterSpec.java:127`：
  ```java
  genParamSpec = new DSAGenParameterSpec(dataTuple.primePLen, dataTuple.subprimeQLen);
  ```

## 4. 测试用例设计 + 自动化实现
- `testWhiteBox()`：断言 `public`、`final`、非接口、父类为 `Object`、实现 `AlgorithmParameterSpec`，方法 `getPrimePLength/getSubprimeQLength/getSeedLength` 存在。
- `testBlackBox()`：两参构造 `(2048,256)` 校验三个 getter（seedLength 默认 256）；三参构造 `(2048,256,512)` 校验 seedLength=512；并断言 `instanceof AlgorithmParameterSpec`。

## 5. 契约深化补充（第 2 批追加）
- 新增 testContract 维度：合法域 {(1024,160),(2048,224),(2048,256),(3072,256)} 与 seedLen>=qLen
- 关键实测：final 类，2/3 参构造（primePLen,primeQLen[,seedLen]）；seedLen 缺省=primeQLen。
AlgorithmParameterGenerator.getInstance("DSA").init(spec) 消费。
