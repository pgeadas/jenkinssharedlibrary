# Scala and sbt Dockerfile
#
# Based on: https://github.com/hseeberger/scala-sbt
#
# Pull base image
# (this will be the 'common-api-cache')
FROM openjdk:11.0.1 as builder

# Env variables
ENV SCALA_VERSION 2.12.8
ENV SBT_VERSION 1.2.8
ENV ROOT_FOLDER backendservices
ENV EVENTLIB libraries/eventlib
ENV COMMONLIB libraries/commonlib

# Install Scala
## Piping curl directly in tar
RUN \
curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
echo >> /root/.bashrc && \
echo "export PATH=~/scala-$SCALA_VERSION/bin:$PATH" >> /root/.bashrc

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install -y --no-install-recommends sbt && \
  rm -rf /var/lib/apt/lists/* \
  sbt sbtVersion && \
  mkdir project && \
  echo "sbt.version=${SBT_VERSION}" >> project/build.properties

COPY project/plugins.sbt project/plugins.sbt

# copies the build.sbt to a temp project, so we build the .ivy cache
# and .repositories and cache them
COPY build.sbt build.sbt
RUN \
echo "case object Temp" > Temp.scala && \
  sbt compile && rm build.sbt && rm -r target && rm -r project/target && rm -r project/project

# does the same thing with the sub-projects, so if we add a dependency
# to a sub-project it does not redownload everything again
COPY $COMMONLIB/build.sbt build.sbt
RUN \
  sbt compile && rm build.sbt && rm -r target && rm -r project/target && rm -r project/project

COPY $EVENTLIB/build.sbt build.sbt
RUN \
  sbt compile && rm build.sbt && rm -r target && rm -r project/target && rm -r project/project

## (this will be the 'api-cache-{project}')
## Ex: api-cache-as or api-cache-atlas

ARG PROJECT_NAME
ENV PROJECT_HOME sites/$PROJECT_NAME

COPY $PROJECT_HOME/build.sbt build.sbt
RUN \
  sbt compile && rm build.sbt && rm -r target && rm -r project/target && rm -r project/project

# creates base folder
WORKDIR .
RUN \
  mkdir -p $ROOT_FOLDER && \
  mkdir -p  $ROOT_FOLDER/$COMMONLIB/project && \
  echo "sbt.version=${SBT_VERSION}" >> $ROOT_FOLDER/$COMMONLIB/project/build.properties && \
  mkdir -p $ROOT_FOLDER/$EVENTLIB/project && \
  echo "sbt.version=${SBT_VERSION}" >> $ROOT_FOLDER/$EVENTLIB/project/build.properties && \
  mkdir -p $ROOT_FOLDER/$PROJECT_HOME/project && \
  echo "sbt.version=${SBT_VERSION}" >> $ROOT_FOLDER/$PROJECT_HOME/project/build.properties && \
  mkdir -p $ROOT_FOLDER/project && \
  echo "sbt.version=${SBT_VERSION}" >> $ROOT_FOLDER/project/build.properties

# copies files that do not change first
COPY $COMMONLIB/project/plugins.sbt $ROOT_FOLDER/$COMMONLIB/project/plugins.sbt
COPY $EVENTLIB/project/plugins.sbt $ROOT_FOLDER/$EVENTLIB/project/plugins.sbt
COPY $PROJECT_HOME/project/plugins.sbt $ROOT_FOLDER/$PROJECT_HOME/project/plugins.sbt
COPY project/plugins.sbt $ROOT_FOLDER/project/plugins.sbt

COPY $COMMONLIB/build.sbt $ROOT_FOLDER/$COMMONLIB/
COPY $EVENTLIB/build.sbt $ROOT_FOLDER/$EVENTLIB/
COPY $PROJECT_HOME/build.sbt $ROOT_FOLDER/$PROJECT_HOME/
COPY build.sbt $ROOT_FOLDER/

# copies shared libs code
COPY $COMMONLIB/app $ROOT_FOLDER/$COMMONLIB/app
COPY $EVENTLIB/app $ROOT_FOLDER/$EVENTLIB/app
COPY $PROJECT_HOME/app $ROOT_FOLDER/$PROJECT_HOME/app
COPY $PROJECT_HOME/conf $ROOT_FOLDER/$PROJECT_HOME/conf
COPY $PROJECT_HOME/public $ROOT_FOLDER/$PROJECT_HOME/public

## Define working directory
WORKDIR $ROOT_FOLDER/

# TODO: packageZipTarball will package all projects. Improve this by building only the needed ones
RUN \
  sbt universal:packageZipTarball && \
  ls && \
  cp $PROJECT_HOME/target/universal/*.tgz /root/app.tgz && \
  rm -r ../$ROOT_FOLDER/

## Pull base image
FROM openjdk:11.0.1-jre-slim-sid as prod

WORKDIR /var/opt/docker/extract

COPY --from=builder /root/app.tgz .

RUN tar -xzf *.tgz && rm *.tgz && mv */* /var/opt/docker/ && rm /var/opt/docker/bin/*.bat && mv /var/opt/docker/bin/* /var/opt/docker/bin/run && rm -r /var/opt/docker/extract/

EXPOSE 9000

ENTRYPOINT ["/var/opt/docker/bin/run"]
