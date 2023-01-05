## Commands

INSTANCE_PROFILE_PREFIX=$(aws cloudformation describe-stacks --stack-name eks-cluster-worker-nodes | jq -r '.Stacks[].Outputs[].ExportName' | sed 's/:.*//')
INSTANCE_PROFILE_NAME=$(aws iam list-instance-profiles | jq -r '.InstanceProfiles[].InstanceProfileName' | grep $INSTANCE_PROFILE_PREFIX)
ROLE_NAME=$(aws iam get-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME | jq -r '.InstanceProfile.Roles[] | .RoleName')

aws iam get-instance-profile --instance-profile-name eks-cluster-worker-nodes-NodeInstanceProfile-17ADSAM6OIWZ

aws iam put-role-policy --role-name eks-cluster-worker-nodes-NodeInstanceRole-X2EV2N47ZGN9 --policy-name ASG-Policy-For-Worker --policy-document file:///Users/abraham/git/bo/kubernetes/cluster-autoscale/k8-asg-policy.json

aws iam get-role-policy --role-name eks-cluster-worker-nodes-NodeInstanceRole-X2EV2N47ZGN9 --policy-name ASG-Policy-For-Worker

aws iam get-role-policy --role-arn arn:aws:iam::828821066173:role/eks-cluster-worker-nodes-NodeInstanceRole-X2EV2N47ZGN9 --policy-name ASG-Policy-For-Worker

