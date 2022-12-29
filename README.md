# Jenkins Shared Library

The goal of having a shared library is to avoid code repetition while separating concerns at the same time.
Each of the four scripts is responsible to execute tasks related to:

1. [Docker](/src/com/pgeadas/DockerUtilities.groovy)
2. [Git](/src/com/pgeadas/GitUtilities.groovy)
3. [Kubernetes](/src/com/pgeadas/K8sUtilities.groovy)
4. [dotNet microservices' repo](/src/com/pgeadas/Microservice.groovy)

The Dockerfile and Jenkinsfile present in the root of this repo should be inside their respective service's module.
When a change in the repository is detected, a build is triggered to either the development or production clusters.

The original project was a dotNet monorepo with several services as modules, so a change in one of them would trigger
all of our services to be rebuilt, needlessly. Since we had limited resources available, triggering all services at
once several times a day, was far from desirable. Thus, with the help of auxiliary repositories, we were able to detect
the exact services that were being changed. For the java backend api, which had its own repo, having auxiliary repos was
not needed.

Under the jenkinsfile's folder, there is a tutorial about how to install Jenkins and with troubleshooting of common
problems faced. However, using a docker image with one pre-installed might be a better option.

Under the aws folder, there is an overview of the Organizational Unit structure implemented in AWS.


