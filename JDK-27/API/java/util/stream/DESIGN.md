# java.util.stream 包（JDK-27 侧） 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

同 JDK-8 面 + takeWhile/dropWhile/mapMulti/toList/ofNullable/iterate 三参、toUnmodifiable*/flatMapping/filtering/teeing、Gatherer 体系（Downstream/Integrator/Greedy）、原语 MapMultiConsumer。

## 2. Java 用户如何使用

toList() 不可变保序；toMap 重复键消息 JDK-9+ 形态（"Duplicate key 1 (attempted merging values a and b)"）与 JDK-8（"Duplicate key a"）差异锁定；windowFixed/windowSliding/fold/scan/mapConcurrent 确定性输出。

## 3. 参考来源

JEP 461 Gatherers；JDK-9/16 流 API 增量。

## 4. 测试用例设计

JDK-8 拷贝 + 各 testJdk27Additions() + Gatherer/Gatherers/Downstream/Integrator/Greedy/MapMultiConsumer×3。
