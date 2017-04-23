# PUT task (update)
resource "aws_api_gateway_method" "tasks_put_method" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "PUT"
    authorization = "NONE"
    
    request_models {
        "application/json" = "${aws_api_gateway_model.task_model.name}"
    }
}

resource "aws_api_gateway_integration" "tasks_put_integration" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_put_method.http_method}"
    passthrough_behavior = "WHEN_NO_TEMPLATES"
    integration_http_method = "POST"
    type = "AWS"
    uri = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.update_task_lambda.arn}/invocations"

    request_templates {
        "application/json" = <<EOF
## There is probably a way I can do this without hardcoding
## them, probably iterating on all properties
{
    "id": "$input.params('id')",
    "user": "$input.path('$.user')",
    "description": "$input.path('$.description')",
    "priority": $input.path('$.priority'),
    "completed": "$input.path('$.completed')"
}
EOF
    }
}

resource "aws_api_gateway_method_response" "tasks_put_200_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_put_method.http_method}"
    status_code = 200
}

resource "aws_api_gateway_integration_response" "tasks_put_200_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_put_method.http_method}"
    status_code = "${aws_api_gateway_method_response.tasks_put_200_response.status_code}"

    depends_on = ["aws_api_gateway_integration.tasks_put_integration"]

    response_templates {
        "application/json" = <<EOF
#set($inputRoot = $input.path('$'))
{ }
EOF
    }
}

resource "aws_api_gateway_method_response" "tasks_put_404_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_put_method.http_method}"
    status_code = 404
}

resource "aws_api_gateway_integration_response" "tasks_put_404_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_put_method.http_method}"
    status_code = "${aws_api_gateway_method_response.tasks_put_404_response.status_code}"
    selection_pattern = ".*ObjectNotFoundException.*"

    depends_on = ["aws_api_gateway_integration.tasks_put_integration"]

    response_templates {
        "application/json" = <<EOF
{ "error": "$input.path('$.errorMessage')" }
EOF
    }
}

resource "aws_api_gateway_method_response" "tasks_put_400_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_put_method.http_method}"
    status_code = 400
}

resource "aws_api_gateway_integration_response" "tasks_put_400_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_put_method.http_method}"
    status_code = "${aws_api_gateway_method_response.tasks_put_400_response.status_code}"
    selection_pattern = ".*ValidationException.*"

    depends_on = ["aws_api_gateway_integration.tasks_put_integration"]

    response_templates {
        "application/json" = <<EOF
{ "error": "$input.path('$.errorMessage')" }
EOF
    }
}

resource "aws_api_gateway_method_response" "tasks_put_500_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_put_method.http_method}"
    status_code = 500
}

resource "aws_api_gateway_integration_response" "tasks_put_500_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_put_method.http_method}"
    status_code = "${aws_api_gateway_method_response.tasks_put_500_response.status_code}"
    selection_pattern = "(\n|.)+"

    depends_on = ["aws_api_gateway_integration.tasks_put_integration"]

    response_templates {
        "application/json" = <<EOF
{ "error": "$input.path('$.errorMessage')" }
EOF
    }
}