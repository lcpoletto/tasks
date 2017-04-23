# POST note (create)
resource "aws_api_gateway_method" "notes_post_method" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.notes_resource.id}"
    http_method = "POST"
    authorization = "NONE"

    request_models {
        "application/json" = "${aws_api_gateway_model.note_model.name}"
    }
}

resource "aws_api_gateway_integration" "notes_post_integration" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.notes_resource.id}"
    http_method = "${aws_api_gateway_method.notes_post_method.http_method}"
    passthrough_behavior = "WHEN_NO_TEMPLATES"
    integration_http_method = "POST"
    type = "AWS"
    uri = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.create_note_lambda.arn}/invocations"

    request_templates {
        "application/json" = <<EOF
{
    "resourceUri": "$context.resourcePath",
    "owner": "$input.path('$.owner')",
    "recipient": "$input.path('$.recipient')",
    "allowChange": "$input.path('$.allowChange')",
    "content": "$input.path('$.content')",
    "updatedBy": "$input.path('$.updatedBy')"
}
EOF
    }
}

resource "aws_api_gateway_method_response" "notes_post_201_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.notes_resource.id}"
    http_method = "${aws_api_gateway_method.notes_post_method.http_method}"
    status_code = 201

    response_parameters {
        "method.response.header.Location" = true
    }
}

resource "aws_api_gateway_integration_response" "notes_post_201_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.notes_resource.id}"
    http_method = "${aws_api_gateway_method.notes_post_method.http_method}"
    status_code = "${aws_api_gateway_method_response.notes_post_201_response.status_code}"

    depends_on = ["aws_api_gateway_integration.notes_post_integration"]

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

resource "aws_api_gateway_method_response" "notes_post_400_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.notes_resource.id}"
    http_method = "${aws_api_gateway_method.notes_post_method.http_method}"
    status_code = 400
}

resource "aws_api_gateway_integration_response" "notes_post_400_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.notes_resource.id}"
    http_method = "${aws_api_gateway_method.notes_post_method.http_method}"
    status_code = "${aws_api_gateway_method_response.notes_post_400_response.status_code}"
    selection_pattern = ".*ValidationException.*"

    depends_on = ["aws_api_gateway_integration.notes_post_integration"]

    response_templates {
        "application/json" = <<EOF
{ "error": "$input.path('$.errorMessage')" }
EOF
    }
}

# catch all mapping for any error that wasn't treated
resource "aws_api_gateway_method_response" "notes_post_500_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.notes_resource.id}"
    http_method = "${aws_api_gateway_method.notes_post_method.http_method}"
    status_code = 500
}

resource "aws_api_gateway_integration_response" "notes_post_500_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.notes_resource.id}"
    http_method = "${aws_api_gateway_method.notes_post_method.http_method}"
    status_code = "${aws_api_gateway_method_response.notes_post_500_response.status_code}"
    selection_pattern = "(\n|.)+"

    depends_on = ["aws_api_gateway_integration.notes_post_integration"]

    response_templates {
        "application/json" = <<EOF
{ "error": "$input.path('$.errorMessage')" }
EOF
    }
}