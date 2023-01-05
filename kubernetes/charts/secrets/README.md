### To create the secrets in kubernetes

helm upgrade -i --namespace development -f values.yaml secrets .

Development

```
cd environment/development
helm upgrade -i --namespace development -f secrets/projectName/values.yaml secrets-development-projectName ../../charts/secrets
```

Production

```
cd environment/production
helm upgrade -i --namespace production -f secrets/projectName/values.yaml secrets-production-projectName ../../charts/secrets
```

Generating keys:
```
openssl genpkey -out mykey.pem -algorithm rsa -pkeyopt rsa_keygen_bits:1024
openssl rsa -in mykey.pem -out mykey.pub -pubout
```
