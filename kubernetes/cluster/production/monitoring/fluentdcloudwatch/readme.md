helm repo add incubator http://storage.googleapis.com/kubernetes-charts-incubator

helm install --namespace monitoring --name fluentd-cloudwatch -f ./fluentd-cloudwatch-values.yaml incubator/fluentd-cloudwatch

kubectl --namespace=monitor get pods -l "app=fluentd-cloudwatch,release=fluentd-cloudwatch"
helm delete --purge fluentd-cloudwatch

https://github.com/aws-samples/aws-workshop-for-kubernetes/blob/master/02-path-working-with-clusters/204-cluster-logging-with-EFK/templates/fluentd-configmap.yaml
https://github.com/helm/charts/blob/98ec381da72f8346d482f962027afc5f775db987/incubator/fluentd-cloudwatch/values.yaml
