#!/bin/bash
#set -x
#trap read debug

echo ""
echo ""
echo ""

echo "\$REVISION             = ${REVISION}"
echo "\$GCPAPPENGINEREPO_URL = ${GCPAPPENGINEREPO_URL}"
echo "\$ORG_NAME             = ${ORG_NAME}"
echo "\$APP_NAME             = ${APP_NAME}"
echo "\$DOCKER_REGISTRY_URL  = ${DOCKER_REGISTRY_URL}"

echo ""
echo ""
echo ""

pushd examples/apps/$APP_NAME

mvn -s ../.m2/settings.xml \
    --batch-mode \
    clean deploy \
    -Dgcpappenginerepo-deploy \
    -Dgcpappenginerepo-deploy.repositoryUrl=$GCPAPPENGINEREPO_URL \
    -Drevision=$REVISION

mvn --batch-mode \
    install \
    -Drevision=$REVISION \
    -Disis.version=$REVISION
    -Dmavenmixin-docker \
    -Ddocker-plugin.imageName=$ORG_NAME/$APP_NAME

mvn -s ../.m2/settings.xml \
    --batch-mode \
    docker:push@push-image-tagged \
    -pl webapp \
    -DskipTests \
    -Dskip.isis-validate \
    -Dskip.isis-swagger \
    -Drevision=$REVISION \
    -Disis.version=$REVISION \
    -Dmavenmixin-docker \
    -Ddocker-plugin.imageName=$ORG_NAME/$APP_NAME \
    -Ddocker-plugin.serverId=docker-registry \
    -Ddocker.registryUrl=$DOCKER_REGISTRY_URL

popd

