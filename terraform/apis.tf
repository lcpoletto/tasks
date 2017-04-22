# looks like terraform does not support manipulating the request validator
# setting on gateway method, will need to go on the API and do this change
# manually for a few methods

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

resource "aws_api_gateway_resource" "notes_resource" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    parent_id = "${aws_api_gateway_rest_api.tasks_api.root_resource_id}"
    path_part = "notes"
}

resource "aws_api_gateway_resource" "note_detail_resource" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    parent_id = "${aws_api_gateway_resource.notes_resource.id}"
    path_part = "{id}"
}

resource "aws_api_gateway_model" "note_model" {
    rest_api_id = "${aws_api_gateway_rest_api.tasks_api.id}"
    name = "note"
    description = "note json schema"
    content_type = "application/json"

    schema = <<EOF
{
    "$schema": "http://json-schema.org/draft-04/schema#",
    "type": "object",
    "title": "A note",
    "properties": {
        "owner": {
            "type": "string",
            "minLength": 5,
            "maxLength": 254,
            "title": "Owner",
            "description": "Note owner's email address"
        },
        "recipient": {
            "type": "string",
            "minLength": 5,
            "maxLength": 254,
            "title": "Recipient",
            "description": "Note recipent's email address"
        },
        "allowChange": {
            "type": "boolean",
            "title": "AllowChange",
            "description": "Allow recipent to update note"
        },
        "content": {
            "type": "string",
            "title": "Content",
            "description": "Note content"
        },
        "updatedBy": {
            "type": "string",
            "minLength": 5,
            "maxLength": 254,
            "title": "UpdatedBy",
            "description": "Email address of user updating the note"
        }
    },
    "required": [
        "owner",
        "recipient",
        "content",
        "allowChange"
    ]
}
EOF
}
