### AWS Organizations
There is only one Master account (like root@example.com).
Then we need one single root account per OrganizationalUnit (OU) (like root+dev@example.com, root+prod@example.com, 
etc) and then add separate users under this account as needed (IAM users, which can be added through: IAM->Users->Add user). 
 
This account should be created under MyOrganization with an alias for the master email (using a + sign). 
One can add more than 1 user under each OU, these will be IAM Accounts and they do not need to be Aliases
for the master/root accounts.

Example AWS Account setup:

    Master Account: root@example.com (there is only 1 master account)
    Root Accounts (1 per OU): root+dev, root+prod, etc. @example.com
    Aliases Accounts: to centralize all the emails for a given environment under 1 email account
    IAM Accounts: As many as needed (IAM->Users->Add user)

We can have more than 1 OU. In our case we had:

    |--root (master account)
       |--Common (1 email per environment)
          |--dev
             |--aws_root_dev@example.com
                (IAM users)
          |--prod
             |--aws_root_prod@example.com
                (IAM users)
       |--Project1 (aliases to the common email)
             |--dev
                |--aws_root_dev+project1@example.com
                   (IAM users)
             |--prod
                |--aws_root_prod+project1@example.com
                   (IAM users)      
       |--Project2 (aliases to the common email)
             |--dev
                |--aws_root_dev+project2@example.com
                   (IAM users)
             |--prod
                |--aws_root_prod+project2@example.com
                   (IAM users) 

### S3 UPDATED WITH OU

Follow the tutorial on how to give access to a bucket from another account:
https://aws.amazon.com/premiumsupport/knowledge-center/cross-account-access-s3/

Create a new IAM user for each environment (dev, prod) and give the respective permissions. 

Pay attention to bucket name restrictions:
https://docs.aws.amazon.com/AmazonS3/latest/dev/BucketRestrictions.html


    Common:
        {environment_prefix}-common-data
    Projects:
        {environment_prefix}-{brand_name}-static
        {environment_prefix}-{brand_name}-data
        www2.{brand_name}.com -> only for prod, for the static pages of affiliates
        
        {environment_prefix} is dev, prod, etc
        {brand_name}: project1, project2, etc ...

### DYNAMODB

All shared tables like ```global system``` should be in Common and not under each project

### LOGS

This is set up in kubernetes and will write to CloudWatch
