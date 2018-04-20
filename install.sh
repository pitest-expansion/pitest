#!/bin/bash

mvn -T 4 install -DskipTests -e -D"checkstyle.skip"=true -Dmaven.javadoc.skip=true verify
read
