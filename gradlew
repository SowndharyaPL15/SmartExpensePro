#!/usr/bin/env sh
GRADLE_OPTS="${GRADLE_OPTS:-""} -Xmx1500m"
APP_HOME="`pwd -P`"
CLASSPATH=$APP_HOME/gradle/wrapper/gradle-wrapper.jar
exec java $GRADLE_OPTS -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
