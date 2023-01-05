# Install

```
 helm install --name efs-provisioner stable/efs-provisioner -f efs-provisioner/values.yaml
 helm upgrade -i efs-provisioner stable/efs-provisioner -f efs-provisioner/values.yaml

```

```
NOTES:
You can provision an EFS-backed persistent volume with a persistent volume claim like below:

kind: PersistentVolumeClaim
apiVersion: v1
metadata:
  name: my-efs-vol-1
  annotations:
    volume.beta.kubernetes.io/storage-class: efs
spec:
  storageClassName: efs
  accessModes:
    - ReadWriteMany
  resources:
    requests:
      storage: 1Mi
```
