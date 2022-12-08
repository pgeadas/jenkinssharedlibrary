package com.pgeadas

class Microservice implements Serializable {
  private final def script
  private final def ku
  private final def du
  private final def gu
  private final def commonServices = ['Common']

  Microservice(def script) {
    this.script = script
    this.du = new DockerUtilities(script)
    this.gu = new GitUtilities(script)
  }

  def getServiceChangesMap(branch, aux_branch) {
    // gets the diff from git
    String changes = gu.getBranchDiff(branch, aux_branch)
    script.echo "Changes:\n${changes}"

    // split lines of the diff first
    def linesList = ((List) changes.trim().split('\n'))
    if (linesList.size() == 0) {
      return
    }
    def changedMap = [:]
    // get the top level folder name, which will be the name of the service we want to check
    //put them in a map, which will also remove duplicates
    for (String line : linesList) {
      try {
        changedMap[line.split('/').take(1)[0]] = true
      } catch (Exception ex) {
        script.echo("Failed to extract Service Name from line: " + line)
      }
    }

    script.echo "changedMap: ${changedMap.size()}"
    return changedMap
  }

  boolean isChanged(serviceName, branch, aux_branch) {
    def changedMap = getServiceChangesMap(branch, aux_branch)

    for (String service in commonServices) {
      script.echo "Checking $service for changes..."
      if (changedMap."$service" != null) {
        script.echo " --> true"
        script.echo " --> Building all services"
        return true
      }
      script.echo " --> false"
    }
    script.echo "Checking $serviceName for changes..."
    boolean result = (changedMap."$serviceName" != null)
    script.echo " --> $result"

    return result
  }

  void testAndPublishResults(cache, image, servicePath) {
    script.stage('test') {
      du.buildImage("--target test --cache-from=$cache -t $image:tester -f $servicePath/Dockerfile .")
      du.publishTestsWithMSTestPublisher("$image:tester", servicePath)
    }
  }

  void buildAndRefreshCache(registryUrl, registryCode, cache, servicePath) {
    script.stage('build cache') {
      try {
        du.pullImage(registryUrl, registryCode, cache)
        du.tagImage("$registryUrl/$cache", cache)
        du.buildImage("--target base --cache-from=$cache -t $cache -f $servicePath/Dockerfile .")
      } catch (ex) {
        script.echo "issue with the cache $ex, then build locally"
        du.buildImage("--target base -t $cache -f $servicePath/Dockerfile .")
      }
    }
    script.stage('upload cache') {
      du.pushImage(registryUrl, registryCode, cache)
    }
  }

}
