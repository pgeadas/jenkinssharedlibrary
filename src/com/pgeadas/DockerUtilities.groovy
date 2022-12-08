package com.pgeadas

class DockerUtilities implements Serializable {

  private final def script

  DockerUtilities(def script) {
    this.script = script
  }

  void pullImage() {
    script.container('docker') {
      script.sh "docker version"
      script.docker.withRegistry("https://$script.registryUrl", script.registryCred) {
        script.docker.image(script.image).pull()
      }
    }
  }

  void pullImage(registryUrl, registryCred, image) {
    script.container('docker') {
      script.sh "docker version"
      script.docker.withRegistry("https://$registryUrl", registryCred) {
        script.docker.image(image).pull()
      }
    }
  }

  void pushImage() {
    script.container('docker') {
      script.docker.withRegistry("https://$script.registryUrl", script.registryCred) {
        script.docker.image(script.image).push()
      }
    }
  }

  void pushImage(registryUrl, registryCred, image) {
    script.container('docker') {
      script.docker.withRegistry("https://$registryUrl", registryCred) {
        script.docker.image(image).push()
      }
    }
  }

  void tagImage(image, tag) {
    script.container('docker') {
      script.sh "docker tag $image  $tag"
    }
  }

  void buildImage(instruction) {
    script.container('docker') {
      script.sh "docker build $instruction"
    }
  }

  void publishTests(image) {
    script.container('docker') {
      script.sh "docker run -d $image"
      script.sh 'CONTAINER_ID=$(docker ps -alq) && docker cp $CONTAINER_ID:/usr/src/app/coverage/ ./coverage && docker cp $CONTAINER_ID:/usr/src/app/test/ ./reports && docker stop $CONTAINER_ID'
      script.sh 'chmod -R a+rwX coverage'
      script.sh 'chmod -R a+rwX reports'
      script.step([$class: 'CoberturaPublisher', coberturaReportFile: 'coverage/cobertura-coverage.xml'])
      script.junit 'reports/**/*.xml'
    }
  }

  void run(instruction) {
    script.container('docker') {
      script.sh "docker run $instruction"
    }
  }

  void publishTestsWithMSTestPublisher(image, servicePath) {
    script.container('docker') {
      script.sh "docker run -d $image"
      script.sh 'CONTAINER_ID=$(docker ps -alq) && docker cp $CONTAINER_ID:/root/' + servicePath + '.Test/TestResults/ ./unit && docker stop $CONTAINER_ID'
      // important give right + make sure that we use .xml extension
      script.sh 'chmod -R a+rwX unit'
      script.step([$class: 'MSTestPublisher', testResultsFile: "unit/**/*.xml", failOnError: true, keepLongStdio: true])
    }
  }
}
