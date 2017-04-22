# POST task (create)
resource "aws_api_gateway_method" "tasks_post_method" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "POST"
    authorization = "NONE"

    request_models {
        "application/json" = "${aws_api_gateway_model.task_model.name}"
    }
}

resource "aws_api_gateway_integration" "tasks_post_integration" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_post_method.http_method}"
    passthrough_behavior = "WHEN_NO_TEMPLATES"
    integration_http_method = "POST"
    type = "AWS"
    uri = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.create_task_lambda.arn}/invocations"

    request_templates {
        "application/json" = <<EOF
{
    "resourceUri": "$context.resourcePath",
    "user": "$input.path('$.user')",
    "description": "$input.path('$.description')",
    "priority": "$input.path('$.priority')",
    "completed": "$input.path('$.completed')"
}
EOF
    }
}

resource "aws_api_gateway_method_response" "tasks_post_201_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_post_method.http_method}"
    status_code = 201

    response_parameters {
        "method.response.header.Location" = true
    }
}

resource "aws_api_gateway_integration_response" "tasks_post_201_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_post_method.http_method}"
    status_code = "${aws_api_gateway_method_response.tasks_post_201_response.status_code}"

    depends_on = ["aws_api_gateway_integration.tasks_post_integration"]

    response_parameters {
        "method.response.header.Location" = "integration.response.body.resourceUri"
    }

    response_templates {
        "application/json" = <<EOF
#set($inputRoot = $input.path('$'))
{ }
EOF
    }
}

resource "aws_api_gateway_method_response" "tasks_post_400_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_post_method.http_method}"
    status_code = 400
}

resource "aws_api_gateway_integration_response" "tasks_post_400_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_post_method.http_method}"
    status_code = "${aws_api_gateway_method_response.tasks_post_400_response.status_code}"
    selection_pattern = ".*ValidationException.*"

    depends_on = ["aws_api_gateway_integration.tasks_post_integration"]

    response_templates {
        "application/json" = <<EOF
{ "error": "$input.path('$.errorMessage')" }
EOF
    }
}