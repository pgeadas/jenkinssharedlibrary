# Jenkins Shared Library

The goal of this library was to avoid code repetition while separating concerns. Each of the four scripts
is responsible to execute tasks related to:

1. [Docker](/src/com/pgeadas/DockerUtilities.groovy)
2. [Git](/src/com/pgeadas/GitUtilities.groovy)
3. [Kubernetes](/src/com/pgeadas/K8sUtilities.groovy)
4. [dotNet microservices' repo](/src/com/pgeadas/Microservice.groovy)

The Dockerfile and Jenkinsfile present in the root of this repo were inside their respective service's module.
When a change in the repository was detected, a build was triggered to either our development or production clusters.

Since we had a monorepo with several services as modules, a change in one of them would trigger all of our services to
rebuild. Because we had limited resources available, triggering all services at once several times a day, was
far from desirable. Thus, with the help of auxiliary repositories, we were able to detect the exact services that were
being changed. For the java backend api that was not needed though.


