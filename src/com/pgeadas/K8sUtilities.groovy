package com.pgeadas

class K8sUtilities implements Serializable {

  private final def script

  K8sUtilities(def script) {
    this.script = script
  }

  void deployAPI(version, environment, project, String service = "backend-api") {
    this.deploy(version, environment, project, service, "environment/$project/values.yaml".toString())
  }

  void deployMicroservice(k8sRepo, version, environment, project, service, gitCredentialsId) {
    script.git branch: 'development', credentialsId: gitCredentialsId, url: "$k8sRepo"
    script.sh "rm -rf charts/$service/environment"
    script.sh "cp -rf environment/$environment/$service/$project charts/$service/environment"
    this.deploy(version, environment, project, service)
  }

  void deploy(version, environment, project, String service) {
    this.deploy(version, environment, project, service, "environment/values.yaml")
  }

  void deploy(version, environment, project, String service, String valuesPath) {
    script.container('kubectl') {
      script.dir("charts/$service") {
        script.sh "helm template . --name $service-$environment-$project --namespace $environment -f $valuesPath"
        script.sh """
                            helm upgrade -i --namespace $environment \
                            -f $valuesPath \
                            --set image.tag=$version \
                            $service-$environment-$project .
                          """
      }
    }
  }

  void inProdCluster(closure) {
    script.container('kubectl') {
      script.withCredentials([
              script.file(credentialsId: 'aws-helm-config', variable: 'AWS_CONFIG_FILE'),
              script.file(credentialsId: 'aws-helm-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE'),
              script.file(credentialsId: 'aws-helm-kube-config-prod', variable: 'KUBECONFIG')]) {
        closure()
      }
    }
  }

  void inDevelopmentCluster(closure) {
    script.container('kubectl') {
      script.withCredentials([
              script.file(credentialsId: 'aws-helm-config', variable: 'AWS_CONFIG_FILE'),
              script.file(credentialsId: 'aws-helm-credentials', variable: 'AWS_SHARED_CREDENTIALS_FILE'),
              script.file(credentialsId: 'aws-helm-kube-config-stage', variable: 'KUBECONFIG')]) {
        closure()
      }
    }
  }

}

