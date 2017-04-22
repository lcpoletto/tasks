variable "access_key" {}
variable "secret_key" {}
variable "region" {}

# this is only here because of SES sandbox and exposing email on 
# the internet is probably not a good idea
variable "mail_from" {}

# constants
variable "package_name" {
	default = "tasks-1.0-SNAPSHOT.zip"
}