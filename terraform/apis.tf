# All lambda permissions are in lambdas.tf file
resource "aws_api_gateway_rest_api" "tasks_api" {
    name = "tasks"
    description = "Task management API."
}

resource "aws_api_gateway_resource" "tasks_resource" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    parent_id = "${aws_api_gateway_rest_api.tasks_api.root_resource_id}"
    path_part = "tasks"
}

resource "aws_api_gateway_resource" "task_detail_resource" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    parent_id = "${aws_api_gateway_resource.tasks_resource.id}"
    path_part = "{id}"
}

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
    integration_http_method = "POST"
    type = "AWS"
    uri = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.retrieve_task_lambda.arn}/invocations"
    
    request_templates {
        "application/json" = <<EOF
""
EOF
    }
}

# POST task (create)
resource "aws_api_gateway_method" "tasks_post_method" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "POST"
    authorization = "NONE"
}

resource "aws_api_gateway_integration" "tasks_post_integration" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_post_method.http_method}"
    integration_http_method = "POST"
    type = "AWS"
    uri = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.create_task_lambda.arn}/invocations"
    
    # no templates so far here
}

# PUT task (update)
resource "aws_api_gateway_method" "tasks_put_method" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "PUT"
    authorization = "NONE"
}

resource "aws_api_gateway_integration" "tasks_put_integration" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_put_method.http_method}"
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

# DELETE task (delete)
resource "aws_api_gateway_method" "tasks_delete_method" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "DELETE"
    authorization = "NONE"
}

resource "aws_api_gateway_integration" "tasks_delete_integration" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_delete_method.http_method}"
    integration_http_method = "POST"
    type = "AWS"
    uri = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.delete_task_lambda.arn}/invocations"
    
    request_templates {
        "application/json" = <<EOF
"$input.params('id')"
EOF
    }
}