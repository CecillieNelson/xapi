#!/bin/sh

mvn clean install -f settings/root.xml -T 2.5C -Dxapi.log.level=INFO -Dxapi.prod=true -Dxapi.debug=false -Dxapi.release=true -Dxapi.skip.test=false $@
