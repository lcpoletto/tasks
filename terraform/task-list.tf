# GET tasks (list)
resource "aws_api_gateway_method" "tasks_get_method" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "GET"
    authorization = "NONE"
}

resource "aws_api_gateway_integration" "tasks_get_integration" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_get_method.http_method}"
    passthrough_behavior = "WHEN_NO_TEMPLATES"
    integration_http_method = "POST"
    type = "AWS"
    uri = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.retrieve_task_lambda.arn}/invocations"

    request_templates {
        "application/json" = <<EOF
""
EOF
    }
}

resource "aws_api_gateway_method_response" "tasks_get_200_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_get_method.http_method}"
    status_code = 200
    
    response_models {
    	"application/json" = "${aws_api_gateway_model.task_model.name}"
    }
}

resource "aws_api_gateway_integration_response" "tasks_get_200_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_get_method.http_method}"
    status_code = "${aws_api_gateway_method_response.tasks_get_200_response.status_code}"

    depends_on = ["aws_api_gateway_integration.tasks_get_integration"]

    response_templates {
        "application/json" = <<EOF
#set($allTasks = $util.parseJson($input.path('$')))
[
#foreach($task in $allTasks)
    {
        "user" : "$task.user",
        "description" : "$task.description",
        "priority" : $task.priority,
        "completed" : "$task.completed"
    }
    #if($foreach.hasNext),#end
#end
]
EOF
    }
}

resource "aws_api_gateway_method_response" "tasks_get_500_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_get_method.http_method}"
    status_code = 500
}

resource "aws_api_gateway_integration_response" "tasks_get_500_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_get_method.http_method}"
    status_code = "${aws_api_gateway_method_response.tasks_get_500_response.status_code}"
    selection_pattern = "(\n|.)+"

    depends_on = ["aws_api_gateway_integration.tasks_get_integration"]

    response_templates {
        "application/json" = <<EOF
{ "error": "$input.path('$.errorMessage')" }
EOF
    }
}