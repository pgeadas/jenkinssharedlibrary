# Create cluster

```
eksctl create cluster --name=production --nodes-min=1 --nodes-max=20 --node-type=m5a.large --region=eu-west-1 --node-labels="autoscaling=enabled,purpose=production" --asg-access  --profile=prod
```

where profile=prod reference your profile in `~/.aws/config`

# Update data plane to the last version

```
eksctl update cluster --name=production --profile=prod --approve
```

In this case 1.13

# Upgrade node groups

take note of the old group nodes

```
eksctl get nodegroups --cluster=production --profile=prod
```

Create a new node group for workers

```
eksctl create nodegroup --cluster=production --profile=prod --name=ng-prod-workers --nodes-min=1 --nodes-max=20  --asg-access --full-ecr-access --node-type=m5a.large --region=eu-west-1 --nodes=1 --node-labels="autoscaling=enabled,purpose=production" --ssh-access --ssh-public-key=eks-ng-workers-key
```

Delete the old one

```
eksctl delete nodegroup --cluster=production --name=ng-ba0f90d8  --profile=prod
```

Upgrade kube-proxy, aws-node and coredns

```
eksctl utils update-kube-proxy --name=production --profile=prod --approve
eksctl utils update-aws-node --name=production  --profile=prod --approve
eksctl utils update-coredns --name=production  --profile=prod --approve
```

# Add users

Edit aws-auth

```
kubectl get configmaps aws-auth -o yaml
```

Add extra users

```
data:
  mapAccounts: |
    - "828821066173"
  mapRoles: |
    - groups:
      - system:bootstrappers
      - system:nodes
      rolearn: arn:aws:iam::786991263349:role/eksctl-production-nodegroup-ng-pr-NodeInstanceRole-1M1S7LJJ23TU4
      username: system:node:{{EC2PrivateDNSName}}
  mapUsers: |
    - userarn: arn:aws:iam::828821066173:user/pgeadas
      username: pgeadas
      groups:
        - system:masters
```

# Install helm

Install helm

```
 k apply -f helm/rbac.yaml
```

Review

```
helm verison
```

# Server metrics

```
 helm install --name metrics-server --namespace monitoring stable/metrics-server
```

# Dashboad

```
kubectl delete ns kubernetes-dashboard
kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0-beta2/aio/deploy/recommended.yaml
```

# Prometheus & Grafna

```
helm repo add coreos https://s3-eu-west-1.amazonaws.com/coreos-charts/stable/
helm install coreos/kube-prometheus --name kube-prometheus --set global.rbacEnable=true --namespace monitoring
```

##  Port binding
Grafana

```
export POD_NAME=$(kubectl get pods --namespace monitoring -l "app=kube-prometheus-grafana,release=kube-prometheus" -o jsonpath="{.items[0].metadata.name}")
kubectl --namespace monitoring port-forward $POD_NAME 3000
```

AlertManager

```
export POD_NAME=$(kubectl get pods --namespace monitoring -l "app=alertmanager" -o jsonpath="{.items[0].metadata.name}")
kubectl --namespace monitoring port-forward $POD_NAME 9093
```

Prometheus

```
export POD_NAME=$(kubectl get pods --namespace monitoring -l "app=prometheus,prometheus=kube-prometheus" -o jsonpath="{.items[0].metadata.name}")
kubectl --namespace monitoring port-forward $POD_NAME 9090
```

# MongoDB

First create the storage class:

```
k apply -f storageclass.yaml
```

Then create a replicaset from the helm chart:

```
helm install --name mongodb-rs stable/mongodb -f values-rs.yaml --namespace mongodb
```

MongoDB can be accessed via port 27017 on the following DNS name from within your cluster:

```
mongodb-rs.mongodb.svc.cluster.local
```

To connect to your database from outside the cluster execute the following command:

```
kubectl port-forward --namespace mongodb svc/mongodb-rs 27017:27017
```
