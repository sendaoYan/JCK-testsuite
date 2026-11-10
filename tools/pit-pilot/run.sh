#!/usr/bin/env bash
# PIT 1.30.1 小型试点：自检测试辅助逻辑（JDK 引导类为被测对象时，PIT 只能变异抽离的应用侧辅助逻辑）。
# 用法：bash run.sh   （在 tools/pit-pilot/ 目录下执行，或任意 cwd 由脚本自行定位）
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$HERE"

JDK="/data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1"
JAVAC="$JDK/bin/javac"
JAVA="$JDK/bin/java"

PIT_CP="lib/pitest-command-line-1.30.1.jar:lib/pitest-entry-1.30.1.jar:lib/pitest-1.30.1.jar:lib/asm-9.11.jar:lib/asm-commons-9.11.jar:lib/commons-text-1.14.0.jar:lib/commons-lang3-3.18.0.jar"

echo "== compile (JDK-27, class major 71) =="
rm -rf classes testclasses pit-out
mkdir -p classes testclasses
"$JAVAC" -d classes src/pitpilot/*.java
"$JAVAC" -cp "classes:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" -d testclasses test/pitpilot/*.java

echo "== baseline green check (JUnit 4.13.2) =="
"$JAVA" -cp "classes:testclasses:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar" \
  org.junit.runner.JUnitCore \
  pitpilot.BigIntegerArithmeticVerifierTest \
  pitpilot.PatternMatcherVerifierTest \
  pitpilot.DecimalFormatHelperTest \
  pitpilot.SortInvariantVerifierTest \
  pitpilot.StringFormatHelperTest \
  pitpilot.CollectionInvariantHelperTest \
  pitpilot.DateTimeHelperTest \
  pitpilot.BitOpsHelperTest

echo "== PIT mutation analysis =="
"$JAVA" -cp "$PIT_CP" org.pitest.mutationtest.commandline.MutationCoverageReport \
  --reportDir pit-out \
  --targetClasses 'pitpilot.*' \
  --targetTests 'pitpilot.*Test' \
  --excludedClasses 'pitpilot.*Test*' \
  --sourceDirs src,test \
  --classPath 'classes,testclasses,lib/junit-4.13.2.jar,lib/hamcrest-core-1.3.jar' \
  --outputFormats HTML,XML \
  --verbose

echo "== done; report: pit-out/index.html + pit-out/mutations.xml =="
