#!/usr/bin/env bash
#
# JCK-testsuite 全量运行脚本（API + JLS + JVMS）。
#
# 被测 JDK 默认为环境变量 JAVA_HOME；可用 JDK=/path/to/jdk 覆盖。
# jtreg 自身需要一个 JDK 11+ 来运行：若被测 JDK 是 JDK-8，
#   请 export JTREG_JAVA=/path/to/jdk17/bin/java。
#
# 用法：
#   cd JDK-27 && ./run-all.sh                      # 全量跑 API+JLS+JVMS
#   cd JDK-27 && ./run-all.sh -e:DISPLAY=:77       # 追加 jtreg 参数（headful/sound 环境注入等）
#   cd JDK-27 && TESTS="API/java/lang" ./run-all.sh   # 只跑某个子目录
#
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$HERE"          # 后续相对路径（TESTS、-nativepath:）都相对本树根
TREE="$(basename "$HERE")"

# ---- 被测 JDK：默认 JAVA_HOME ----
JDK="${JDK:-${JAVA_HOME:-}}"
if [ -z "$JDK" ]; then
  echo "error: 未指定被测 JDK。请 export JAVA_HOME=<jdk> 或 JDK=<jdk>" >&2
  exit 1
fi
if [ ! -x "$JDK/bin/java" ] || [ ! -x "$JDK/bin/javac" ]; then
  echo "error: 找不到 $JDK/bin/java 或 $JDK/bin/javac（需要完整 JDK）" >&2
  exit 1
fi

# ---- jtreg 工具链 ----
JTREG="${JTREG:-/data/yansendao/JDK/tools/jtreg/jtreg-8.3/bin/jtreg}"
# jtreg 运行器（自身 JVM）：要求 JDK 11+；默认用被测 JDK，JDK-8 场景请用 JTREG_JAVA 覆盖。
JTREG_JAVA="${JTREG_JAVA:-$JDK/bin/java}"

# ---- 测试范围：默认全量，可用 TESTS 覆盖 ----
if [ -n "${TESTS:-}" ]; then
  read -ra TEST_DIRS <<< "$TESTS"
else
  TEST_DIRS=("$HERE/API" "$HERE/JLS" "$HERE/JVMS")
fi

# ---- 工作/报告目录落 /tmp，不污染仓库 ----
STAMP="$(date +%Y%m%d-%H%M%S)"
W="/tmp/jck-$TREE-$STAMP/w"
R="/tmp/jck-$TREE-$STAMP/r"
mkdir -p "$W" "$R"

echo "=== JCK-testsuite $TREE 全量运行 ==="
echo "被测 JDK    : $JDK"
echo "jtreg 运行器: $JTREG_JAVA"
echo "测试范围    : ${TEST_DIRS[*]}"
echo "工作/报告   : $W / $R"

export JTREG_JAVA
"$JTREG" -jdk:"$JDK" -w "$W" -r "$R" -verbose:summary "${TEST_DIRS[@]}" "$@"
