# Install ingress controller

`helm upgrade -i traefik -f values.yaml stable/traefik`

# To check information about the Service

```kubectl get service traefik```

```
NAME      TYPE           CLUSTER-IP     EXTERNAL-IP                                                              PORT(S)                                     AGE
traefik   LoadBalancer   10.100.112.5   a0901e11fa70311e9ae780ade953baea-556385587.eu-west-1.elb.amazonaws.com   80:30279/TCP,443:32695/TCP,8080:32618/TCP   130m
```
