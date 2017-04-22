# looks like terraform does not support manipulating the request validator
# setting on gateway method, will need to go on the API and do this change
# manually for a few methods

# TODO: Return HTTP 400 "Invalid request body" when there is validation errors

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

resource "aws_api_gateway_model" "task_model" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    name = "task"
    description = "task json schema"
    content_type = "application/json"

    schema = <<EOF
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "type": "object",
    "title": "A task",
    "properties": {
        "user": {
            "type": "string",
            "minLength": 5,
            "maxLength": 254,
            "title": "User",
            "description": "User's email address"
        },
        "description": {
            "type": "string",
            "minLength": 1,
            "title": "Description of the task"
        },
        "priority": {
            "type": "integer",
            "multipleOf": 1,
            "maximum": 9,
            "minimum": 0,
            "title": "Priority",
            "description": "Task priority, as a single-digit integer. 0 is highest priority"
        },
        "completed": {
            "type": "string",
            "format": "date-time",
            "title": "Completed",
            "description": "Completed datetime, formatted as an ISO8601 string"
        }
    },
    "required": [
        "description",
        "priority"
    ]
}
EOF
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
}

resource "aws_api_gateway_integration_response" "tasks_get_200_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.tasks_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_get_method.http_method}"
    status_code = "${aws_api_gateway_method_response.tasks_get_200_response.status_code}"

    depends_on = ["aws_api_gateway_integration.tasks_get_integration"]

    # TODO: need to figure out how to convert the date into ISO8601
    response_templates {
        "application/json" = <<EOF
#set($allTasks = $input.path('$'))
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
    integration_http_method = "POST"
    type = "AWS"
    uri = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.create_task_lambda.arn}/invocations"

    request_templates {
        "application/json" = <<EOF
{
    "resourceUri": "$context.resourcePath",
    "user": "$input.path('$.user')",
    "description": "$input.path('$.description')",
    "priority": $input.path('$.priority'),
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
    passthrough_behavior = "WHEN_NO_TEMPLATES"
    integration_http_method = "POST"
    type = "AWS"
    uri = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.delete_task_lambda.arn}/invocations"

    request_templates {
        "application/json" = <<EOF
"$input.params('id')"
EOF
    }
}

resource "aws_api_gateway_method_response" "tasks_delete_200_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.task_detail_resource.id}"
    http_method = "${aws_api_gateway_method.tasks_delete_method.http_method}"
    status_code = 200
}
