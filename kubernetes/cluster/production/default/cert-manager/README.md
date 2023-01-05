## Github repo
https://github.com/helm/charts/tree/master/stable/cert-manager

## Helm docs (based on here)
https://docs.cert-manager.io/en/latest/getting-started/install/kubernetes.html

## Steps

# Install the CustomResourceDefinition resources separately
```kubectl apply -f https://raw.githubusercontent.com/jetstack/cert-manager/release-0.8/deploy/manifests/00-crds.yaml```

# Create the namespace for cert-manager
```kubectl create namespace default```

# Label the cert-manager namespace to disable resource validation
```kubectl label namespace default certmanager.k8s.io/disable-validation=true```

# Add the Jetstack Helm repository
```helm repo add jetstack https://charts.jetstack.io```

# Update your local Helm chart repository cache
```helm repo update```

# Install the cert-manager Helm chart
```
helm install \
  --name cert-manager \
  --namespace default \
  --version v0.8.1 \
  jetstack/cert-manager
```

### Test if everything went ok (optional)
# Create a ClusterIssuer to test the webhook works okay
```
cat <<EOF > test-resources.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: cert-manager-test
---
apiVersion: certmanager.k8s.io/v1alpha1
kind: Issuer
metadata:
  name: test-selfsigned
  namespace: cert-manager-test
spec:
  selfSigned: {}
---
apiVersion: certmanager.k8s.io/v1alpha1
kind: Certificate
metadata:
  name: selfsigned-cert
  namespace: cert-manager-test
spec:
  commonName: example.com
  secretName: selfsigned-cert-tls
  issuerRef:
    name: test-selfsigned
EOF
```

# Create the test resources

```kubectl apply -f test-resources.yaml```

# Check the status of the newly created certificate
# You may need to wait a few seconds before cert-manager processes the
# certificate request

```kubectl describe certificate -n cert-manager-test```

```
...
Spec:
  Common Name:  example.com
  Issuer Ref:
    Name:       test-selfsigned
  Secret Name:  selfsigned-cert-tls
Status:
  Conditions:
    Last Transition Time:  2019-01-29T17:34:30Z
    Message:               Certificate is up to date and has not expired
    Reason:                Ready
    Status:                True
    Type:                  Ready
  Not After:               2019-04-29T17:34:29Z
Events:
  Type    Reason      Age   From          Message
  ----    ------      ----  ----          -------
  Normal  CertIssued  4s    cert-manager  Certificate issued successfully

```

# Clean up the test resources
```kubectl delete -f test-resources.yaml```

# Apply the issuer.yaml
```kubectl apply -f issuer.yaml```

# Test if everything went smoothly
```kubectl describe certificates -n production```


```
Name:         backend-api-production
Namespace:    production
Labels:       <none>
Annotations:  <none>
API Version:  certmanager.k8s.io/v1alpha1
Kind:         Certificate
Metadata:
  Creation Timestamp:  2019-07-15T14:51:35Z
  Generation:          4
  Resource Version:    1311784
  Self Link:           /apis/certmanager.k8s.io/v1alpha1/namespaces/production/certificates/backend-api-production
  UID:                 0ad116f7-a710-11e9-ae78-0ade953baea4
Spec:
  Acme:
    Config:
      Domains:
        prodapi.example.com
      http01:
        Ingress Class:  traefik
  Dns Names:
    prodapi.example.com
  Issuer Ref:
    Kind:       ClusterIssuer
    Name:       letsencrypt-prod
  Secret Name:  backend-api-production-crt-secret
Status:
  Conditions:
    Last Transition Time:  2019-07-15T15:04:22Z
    Message:               Certificate is up to date and has not expired
    Reason:                Ready
    Status:                True
    Type:                  Ready
  Not After:               2019-10-13T14:04:20Z
Events:
  Type     Reason              Age                From          Message
  ----     ------              ----               ----          -------
  Warning  IssuerNotFound      16m (x2 over 16m)  cert-manager  clusterissuer.certmanager.k8s.io "letsencrypt-prod" not found
  Warning  IssuerNotReady      3m57s              cert-manager  Issuer letsencrypt-prod not ready
  Normal   Generated           3m55s              cert-manager  Generated new private key
  Normal   GenerateSelfSigned  3m55s              cert-manager  Generated temporary self signed certificate
  Normal   OrderCreated        3m55s              cert-manager  Created Order resource "backend-api-production-1527063111"
  Normal   OrderComplete       3m29s              cert-manager  Order "backend-api-production-1527063111" completed successfully
  Normal   CertIssued          3m29s              cert-manager  Certificate issued successfully
```


In the case above, we had the name of the certmanager wrong, so that is why we see the message "IssuerNotFound". 
To fix it, we changed the following:

```
apiVersion: certmanager.k8s.io/v1alpha1
kind: ClusterIssuer
metadata:
  name: letsencrypt-prod
...
```
