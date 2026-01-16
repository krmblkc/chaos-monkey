#!/bin/bash
# Maven Wrapper Downloader Script

MAVEN_WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
MAVEN_WRAPPER_PROPERTIES_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper.properties"

mkdir -p .mvn/wrapper
curl -sL "$MAVEN_WRAPPER_URL" -o .mvn/wrapper/maven-wrapper.jar
echo "distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip" > .mvn/wrapper/maven-wrapper.properties
echo "wrapperUrl=$MAVEN_WRAPPER_URL" >> .mvn/wrapper/maven-wrapper.properties
