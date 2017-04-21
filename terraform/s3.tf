resource "aws_s3_bucket" "deploy_bucket" {
	bucket = "lcpoletto_tasks_deploy_bucket"
	acl = "private"
	force_destroy = true
	
	versioning {
		enabled = true
	}
}

resource "aws_s3_bucket_object" "lambda_package" {
	bucket = "${aws_s3_bucket.deploy_bucket.bucket}"
	source = "../build/distributions/${var.package_name}"
	key = "${var.package_name}"
	etag = "${md5(file("../build/distributions/${var.package_name}"))}"
}