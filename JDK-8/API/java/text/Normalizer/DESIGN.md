# java.text.Normalizer 测试设计案（JDK-8）

> 覆盖目标：完整契约（final 工具类、无公开构造器、NFC/NFD/NFKC/NFKD、isNormalized、幂等、边界、null），
> 非冒烟。

## 1. API 提供了哪些功能

- 静态工具：`normalize(CharSequence, Form)`、`isNormalized(CharSequence, Form)`。
- 形式枚举：`Normalizer.Form`：`NFC`、`NFD`、`NFKC`、`NFKD`。
- `final` 类，无公开构造器。

## 2. Java 用户如何使用

- `Normalizer.normalize(text, Form.NFC)` 做 Unicode 规范化；`isNormalized(text, Form.NFD)` 判断。
- NFC=规范合成、NFD=规范分解、NFKC/NFKD 额外做兼容映射（连字、全角、上标）。

## 3. 参考来源（不凭记忆）

- Oracle Java SE 8 javadoc + `~/git/jdk8u-dev/jdk/src/share/classes/java/text/Normalizer.java`
  （委托 `sun.text.normalizer.NormalizerBase`）。
- JDK-27 同目录 DESIGN.md（行为一致）。

## 4. 测试用例设计（映射到 NormalizerTest.java）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 白盒：final、无公开构造器、normalize/isNormalized 形态 | testWhiteBox |
| P0 | NFC/NFD 规范合成/分解往返 | testCanonicalComposition |
| P0 | NFKC/NFKD 兼容映射（连字/全角/上标）+ NFC 不做兼容 | testCompatibilityComposition |
| P0 | isNormalized 各形式真假 | testIsNormalized |
| P0 | NFC/NFD 幂等 + normalize/isNormalized 的 null→NPE | testIdempotentAndNull |
| P1 | 空串/ASCII 边界 + 各形式幂等 | testBoundaryValues |

## 关键规范细节（易错，已锁入断言）

- `normalize(null,...)`/`isNormalized(null,...)`/`normalize(x,null)`/`isNormalized(x,null)` 均抛
  `NullPointerException`。
- NFC 不做兼容映射（`ﬃ` 在 NFC 下保持不变，NFKC/NFKD 下才分解为 `ffi`）。

## 注意事项

- 纯文本固定；无 Locale/墙钟/随机/全局副作用；headless-safe 不加 `@key`。
