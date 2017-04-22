# Task Management API
This project defines a Task Management API using Java, AWS Lambda, AWS Api Gateware and terraform.

## Getting Started
After you clone the repo you will need to package the project by using Gradle:

```Shell
$ ./gradle build
```

The command above will run all the unit tests and packaging tasks and will generate an AWS Lambda package in this location: `./build/distributions/tasks-1.0-SNAPSHOT.zip`.

### Deploying to AWS
After a succesfull build you can use terraform to deploy the generated package to AWS, in order to do that it's needed to configure a few variables to be passed to terraform. There are many ways to do that, below you can find one example:

```Shell
#!/usr/bin/env bash
export AWS_ACCESS_KEY_ID="<your_access_key>"
export AWS_SECRET_ACCESS_KEY="<your_secret_key>"
export AWS_DEFAULT_REGION="<default_aws_region>"
export TF_VAR_access_key="${AWS_ACCESS_KEY_ID}"
export TF_VAR_secret_key="${AWS_SECRET_ACCESS_KEY}"
export TF_VAR_region="${AWS_DEFAULT_REGION}"
export TF_VAR_mail_from="<validated_aws_SES_email>"
```

After configuring the variables you can go on `terraform` directory and run `terraform plan` and `terraform apply` if your plan seems ok.

If everything goes well you will have a fully working REST API on AWS on a serverless design.

Have fun!!!
