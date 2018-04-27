#!/bin/bash

mvn -T 4 clean install -DskipTests -e -D"checkstyle.skip"=true -Dmaven.javadoc.skip=true verify
read
