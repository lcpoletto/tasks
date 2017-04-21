resource "aws_lambda_function" "create_task_lambda" {
	function_name = "create_task"
	handler = "com.lcpoletto.tasks.CreateTask::handleRequest"
	role = "${aws_iam_role.rw_tasks_role.arn}"
	runtime = "java8"
	timeout = "15"
	memory_size = "512"
	
	s3_bucket = "${aws_s3_bucket.deploy_bucket.bucket}"
	s3_key = "${var.package_name}"
}

resource "aws_lambda_function" "delete_task_lambda" {
	function_name = "delete_task"
	handler = "com.lcpoletto.tasks.DeleteTask::handleRequest"
	role = "${aws_iam_role.rw_tasks_role.arn}"
	runtime = "java8"
	timeout = "15"
	memory_size = "512"
	
	s3_bucket = "${aws_s3_bucket.deploy_bucket.bucket}"
	s3_key = "${var.package_name}"
}

resource "aws_lambda_function" "retrieve_task_lambda" {
	function_name = "retrieve_task"
	handler = "com.lcpoletto.tasks.RetrieveTask::handleRequest"
	role = "${aws_iam_role.ro_tasks_role.arn}"
	runtime = "java8"
	timeout = "15"
	memory_size = "512"
	
	s3_bucket = "${aws_s3_bucket.deploy_bucket.bucket}"
	s3_key = "${var.package_name}"
}

resource "aws_lambda_function" "send_task_lambda" {
	function_name = "send_task"
	handler = "com.lcpoletto.tasks.SendTask::handleRequest"
	role = "${aws_iam_role.send_tasks_role.arn}"
	runtime = "java8"
	timeout = "15"
	memory_size = "512"
	
	s3_bucket = "${aws_s3_bucket.deploy_bucket.bucket}"
	s3_key = "${var.package_name}"
}

resource "aws_lambda_function" "update_task_lambda" {
	function_name = "update_task"
	handler = "com.lcpoletto.tasks.UpdateTask::handleRequest"
	role = "${aws_iam_role.rw_tasks_role.arn}"
	runtime = "java8"
	timeout = "15"
	memory_size = "512"
	
	s3_bucket = "${aws_s3_bucket.deploy_bucket.bucket}"
	s3_key = "${var.package_name}"
}