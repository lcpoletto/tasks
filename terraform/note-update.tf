# PUT note (update)
resource "aws_api_gateway_method" "notes_put_method" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.note_detail_resource.id}"
    http_method = "PUT"
    authorization = "NONE"
    
    request_models {
        "application/json" = "${aws_api_gateway_model.note_model.name}"
    }
}

resource "aws_api_gateway_integration" "notes_put_integration" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.note_detail_resource.id}"
    http_method = "${aws_api_gateway_method.notes_put_method.http_method}"
    passthrough_behavior = "WHEN_NO_TEMPLATES"
    integration_http_method = "POST"
    type = "AWS"
    uri = "arn:aws:apigateway:${var.region}:lambda:path/2015-03-31/functions/${aws_lambda_function.update_note_lambda.arn}/invocations"

    request_templates {
        "application/json" = <<EOF
## There is probably a way I can do this without hardcoding
## them, probably iterating on all properties
{
    "id": "$input.params('id')",
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

resource "aws_api_gateway_method_response" "notes_put_200_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.note_detail_resource.id}"
    http_method = "${aws_api_gateway_method.notes_put_method.http_method}"
    status_code = 200
}

resource "aws_api_gateway_integration_response" "notes_put_200_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.note_detail_resource.id}"
    http_method = "${aws_api_gateway_method.notes_put_method.http_method}"
    status_code = "${aws_api_gateway_method_response.notes_put_200_response.status_code}"

    depends_on = ["aws_api_gateway_integration.notes_put_integration"]

    response_templates {
        "application/json" = <<EOF
#set($inputRoot = $input.path('$'))
{ }
EOF
    }
}

resource "aws_api_gateway_method_response" "notes_put_404_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.note_detail_resource.id}"
    http_method = "${aws_api_gateway_method.notes_put_method.http_method}"
    status_code = 404
}

resource "aws_api_gateway_integration_response" "notes_put_404_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.note_detail_resource.id}"
    http_method = "${aws_api_gateway_method.notes_put_method.http_method}"
    status_code = "${aws_api_gateway_method_response.notes_put_404_response.status_code}"
    selection_pattern = ".*ObjectNotFoundException.*"

    depends_on = ["aws_api_gateway_integration.notes_put_integration"]

    response_templates {
        "application/json" = <<EOF
{ "error": "$input.path('$.errorMessage')" }
EOF
    }
}

resource "aws_api_gateway_method_response" "notes_put_403_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.note_detail_resource.id}"
    http_method = "${aws_api_gateway_method.notes_put_method.http_method}"
    status_code = 403
}

resource "aws_api_gateway_integration_response" "notes_put_403_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.note_detail_resource.id}"
    http_method = "${aws_api_gateway_method.notes_put_method.http_method}"
    status_code = "${aws_api_gateway_method_response.notes_put_403_response.status_code}"
    selection_pattern = ".*PermissionException.*"

    depends_on = ["aws_api_gateway_integration.notes_put_integration"]

    response_templates {
        "application/json" = <<EOF
{ "error": "$input.path('$.errorMessage')" }
EOF
    }
}

resource "aws_api_gateway_method_response" "notes_put_400_response" {
	rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.note_detail_resource.id}"
    http_method = "${aws_api_gateway_method.notes_put_method.http_method}"
    status_code = 400
}

resource "aws_api_gateway_integration_response" "notes_put_400_integration_response" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    resource_id = "${aws_api_gateway_resource.note_detail_resource.id}"
    http_method = "${aws_api_gateway_method.notes_put_method.http_method}"
    status_code = "${aws_api_gateway_method_response.notes_put_400_response.status_code}"
    selection_pattern = ".*ValidationException.*"

    depends_on = ["aws_api_gateway_integration.notes_put_integration"]

    response_templates {
        "application/json" = <<EOF
{ "error": "$input.path('$.errorMessage')" }
EOF
    }
}