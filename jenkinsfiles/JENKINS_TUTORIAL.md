# Installing Jenkins on EC2 behind a LoadBalancer

In this tutorial, we will install Jenkins as our automation tool. In the end, you should be able to automatically launch a new version of your website online just by doing a push of code to Bitbucket.

# Pre-requisites

This tutorial assumes you already own and know how to create EC2 instances on AWS and have Unix experience. The instance should be at least an ec2.small one, the ec2.micro does not handle Jenkins jobs without running out of memory.

### Step 1 - Update the system

Login to your Ubuntu 16.04 via SSH as user root

	sudo su 

Then update the system

	apt-get upgrade && apt-get install

After doing that, we can install Jenkins in our machine which is described in the next step.

	
### Step 2 - Installing Jenkins

Run the following command to add Jenkins key to the server

	wget -q -O - https://pkg.jenkins.io/debian/jenkins-ci.org.key | sudo apt-key add -

Once you add the key, add the Jenkins repository too

	echo 'deb https://pkg.jenkins.io/debian-stable binary/' | tee -a /etc/apt/sources.list

Update the repositories

	apt-get update

On Debian based distros such as Ubuntu, Jenkins can be installed through the ‘apt-get’ package manager. It will install Jenkins, Java version 8 and all necessary dependencies.

	apt-get install jenkins

After this, Jenkins should already be running and we can start configuring it.

### Step 3 - Configuring Jenkins

##### Connecting to Jenkins
To connect to our Jenkins server we just need to open port 8080 in the server instance and/or open this port in the firewall. After that, we should be able to connect to the ```ip:port``` in cause via HTTP only. If this OK for you, you can skip to the next sub-step. I would however encourage you to set up a secure HTTPS connection to the server.

##### Connecting through HTTPS using an AWS load balancer
If you want to be able to connect using HTTPS and avoid configuring your own webserver we can use a Load Balancer in AWS to forward the traffic to the Jenkins EC2 instance from HTTP to HTTPS doing the following steps:

1. Add an entry in ```Route 53``` with the Public DNS of the Jenkins instance as a CNAME to the domain used;
2. Create a security group with the ports that should be opened (8080, 443 and only allow access for ips inside the VPC);
3. Add an instance target group with the instances and ports where the traffic should be forward to;
4. Add the same ports as listeners when setting up the load balancer;
5. Use the certificate for the domain where you want to use the instance at.

After doing this, connecting to Jenkins through HTTP will not be allowed and all the requests will go through HTTPS.


##### Unlocking Jenkins
You can access your Jenkins at port 8080 (or other custom port you selected before). You will see the next screen the first time you access it:

![Alt text](https://drive.google.com/file/d/0B1SaNSoS7WULQUw4dXhkMTRrRm8/view)

To get the initial password, do: 
	
	$ sudo cat /var/lib/jenkins/secrets/initialAdminPassword  
	
copy the password and insert that in the Jenkins initial page. 

##### Installing Jenkins plugins

In the next screen, select "install suggested plugin". Wait until the plugins are installed and then create the first Admin User. After creating an Admin user, you should be able to log in into Jenkins and start building jobs! If there are more dependencies on other plugins, these can also be installed after logging in or from the Ubuntu instance itself.


### Step 4 - Installing dependencies for backend API

##### Add jenkins user to the sudoers group
To be able to deploy and run our projects, external dependencies need to be installed, otherwise Jenkins will not be able to build them. In order to do that, we need to install a set of dependencies under the user ```jenkins``` that was automatically created when we installed jenkins. To do this, we should temporarily allow the user jenkins sudo rights

	sudo su    
	visudo -f /etc/sudoers

add add following line at the end of this file:

	jenkins ALL= NOPASSWD: ALL

**NOTE:** *Remove this line when everything is done.*


##### Install the dependencies
Install everything under the user ```jenkins```:

Install nodeJS: 

	sudo apt-get install nodejs 

and create symlink for it: 

	sudo ln -s /usr/bin/nodejs /usr/bin/node

Install npm: 

	sudo apt-get install npm
	install apidoc: sudo npm install apidoc -g

Install sbt:

	echo "deb https://dl.bintray.com/sbt/debian /" | sudo tee -a /etc/apt/sources.list.d/sbt.list
	sudo apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823
	sudo apt-get update
	sudo apt-get install sbt

Install aws cli:

	sudo apt install python-pip
	sudo -H pip install --upgrade pip
	sudo pip install awscli --upgrade --user

and create sym link for it:

	sudo ln -s ~/.local/bin/aws /usr/local/bin  

Check if ```aws``` is correctly installed

	aws --version

Configure aws credentials and region (actually it did not work for the region, I needed to add this manually later, but this should work according to AWS...)

	sudo aws configure

Make sure java and javac are detected

	java -version
	javac -version

If javac is not yet installed, install it

	sudo apt-get install openjdk-8-jdk


### Step 5 - Add an export command to your profile script (set java_home, aws, etc)

	export PATH=~/.local/bin:$PATH

This command adds a path (~/.local/bin in this example) to the current PATH variable. However, to make changes like this persistent and not only available for the current session, we need to change not the ```/etc/environment``` file but, the ```/etc/profile``` .

Add java, aws to the PATH by opening

	sudo nano /etc/profile

Add to the end of the file:
	
	JAVA_HOME=/usr/lib/jvm/java-8-openjdk
	export JAVA_HOME
	export PATH=~/bin:$PATH
	export PATH=~/.local/bin:$PATH
	export PATH
	AWS_REGION=eu-central-1
	export AWS_REGION


### Troubleshoot

##### pip error
During pip installation, I have got an error

	jenkins@ip-xxx-xx-xx-xxx:~$ pip install --upgrade pip
	Collecting pip
  	Downloading pip-10.0.0-py2.py3-none-any.whl (1.3MB)
    100% |████████████████████████████████| 1.3MB 1.0MB/s
	Installing collected packages: pip
	Successfully installed pip-8.1.1
	You are using pip version 8.1.1, however version 10.0.0 is available.
	You should consider upgrading via the 'pip install --upgrade pip' command.
	jenkins@ip-xxx-xx-xx-xxx:~$ pip install awscli --upgrade --user
	Traceback (most recent call last):
  	File "/usr/bin/pip", line 9, in <module>
    from pip import main
	ImportError: cannot import name main

solved by doing  

	sudo easy_install -U pip


##### Folder permissions error
During some configuration step, I add Ubuntu complaining about folder permissions, which were solved doing

	sudo chown -R username:group directory

which will change ownership (both user and group) of all files and directories inside of directory and directory itself.

If you do 

	sudo chown username:group directory

will only change the permission of the folder directory but will leave the files and folders inside the directory alone.

In order to check who is the owner of the folder or file, one can do

	stat /folder/or/file

##### Buildscripts error
While trying to get the ```buildscripts``` project running, I got an error

	org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: Scripts not permitted to use method groovy.lang.GroovyObject invokeMethod java.lang.String java.lang.Object (org.codehaus.groovy.runtime.GStringImpl call org.codehaus.groovy.runtime.GStringImpl)
    at org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.StaticWhitelist.rejectMethod(StaticWhitelist.java:165)
    at org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SandboxInterceptor.onMethodCall(SandboxInterceptor.java:117)
    at org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SandboxInterceptor.onMethodCall(SandboxInterceptor.java:103)
    at org.kohsuke.groovy.sandbox.impl.Checker$1.call(Checker.java:149)
    at org.kohsuke.groovy.sandbox.impl.Checker.checkedCall(Checker.java:146)
    at com.cloudbees.groovy.cps.sandbox.SandboxInvoker.methodCall(SandboxInvoker.java:15)
    at WorkflowScript.run(WorkflowScript:71)
    at ___cps.transform___(Native Method)
	(...)


There was a pending command, which needed to be approved:
	
	Navigate to jenkins > Manage jenkins > In-process Script Approval

##### nodeJS and npm errors
If something goes wrong about these two, there is a nice tutorial about how to fix or re-install them.

	https://www.digitalocean.com/community/tutorials/how-to-install-node-js-on-ubuntu-16-04 

##### node_modules error
While setting the jobs to run in Jenkins, got an error about ```node_modules```.

To solve that, I needed to

	sudo rm -rf node_modules
	sudo npm install

An alternative to this, in case the first method does not work, would be

	sudo npm uninstall gulp-imagemin
	sudo npm install gulp-imagemin

##### .zip not found error

	/var/lib/jenkins/workspace/project@tmp/durable-c44b0d06/script.sh: 2: /var/lib/jenkins/workspace/projectw@tmp/durable-c44b0d06/script.sh: zip: not found

Make sure zip is installed. If the error persists, is because it is the first deploy of the app but the folder already exists, so the file is actually not inside the folder. Just delete the folder.

(TODO: Check for a better way of checking if "isFirstDeploy" (prob we can just remove the folder when this error is detected. Right now we are just failing when the error happens.)

##### git tip revision error
The error here was

	ERROR: Could not determine exact tip revision of dev-server; falling back to nondeterministic checkout
	
In order to solve this error that suddenly started to be thrown when doing "Build now" from Jenkins, was just saving the file again and push it again to the remote repository.
The error was reported before here

	https://issues.jenkins-ci.org/browse/JENKINS-48571
	
	
##### InvalidClientTokenId error
When setting up the job in jenkins, if the credentials are not properly set, we will see this error

    An error occurred (InvalidClientTokenId) when calling the DescribeEnvironmentHealth operation: The security token included in the request is invalid.
    
The solution is opening 

    /var/lib/jenkins/.aws/credentials
    
and update the credentials under the right jenkins user.   

### Step 6 - Setting Bitbucket hook

### Step 7 - Setting email notifications
To set the mail notifications, we need to go to the ```@example``` gmail account and add an ```app password``` there.
After generating a password, we need to add it in jenkins while configuring the email smtp.

