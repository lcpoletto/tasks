resource "aws_api_gateway_deployment" "tasks_dev_deploy" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
	description = "Development deploy of Task Management API."
	stage_name = "development"
	stage_description = "Development environment for Task management API."
	
	depends_on = [
		"aws_api_gateway_integration.tasks_post_integration",
		"aws_api_gateway_integration.tasks_delete_integration",
		"aws_api_gateway_integration.tasks_get_integration",
		"aws_api_gateway_integration.tasks_put_integration"
	]
}