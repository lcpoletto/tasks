resource "aws_iam_role" "ro_tasks_role" {
	name = "ro_tasks_role"
	
	assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Effect": "Allow",
            "Principal": {
                "Service": "lambda.amazonaws.com"
            }
        }
    ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "attach_dynamo_ro_tasks" {
    role       = "${aws_iam_role.ro_tasks_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "attach_cloudwatch_ro_tasks" {
    role       = "${aws_iam_role.ro_tasks_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

resource "aws_iam_role" "rw_tasks_role" {
	name = "rw_tasks_role"
	
	assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Effect": "Allow",
            "Principal": {
                "Service": "lambda.amazonaws.com"
            }
        }
    ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "attach_dynamo_rw_tasks" {
    role       = "${aws_iam_role.rw_tasks_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
}

resource "aws_iam_role_policy_attachment" "attach_cloudwatch_rw_tasks" {
    role       = "${aws_iam_role.rw_tasks_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

resource "aws_iam_role" "send_tasks_role" {
	name = "send_tasks_role"
	
	assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Effect": "Allow",
            "Principal": {
                "Service": "lambda.amazonaws.com"
            }
        }
    ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "attach_dynamo_sender_tasks" {
    role       = "${aws_iam_role.send_tasks_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "attach_cloudwatch_sender_tasks" {
    role       = "${aws_iam_role.send_tasks_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

resource "aws_iam_role_policy_attachment" "attach_ses_sender_tasks" {
    role       = "${aws_iam_role.send_tasks_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonSESFullAccess"
}

resource "aws_iam_role" "send_note_update_role" {
	name = "send_note_update_role"
	
	assume_role_policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Action": "sts:AssumeRole",
            "Effect": "Allow",
            "Principal": {
                "Service": "lambda.amazonaws.com"
            }
        }
    ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "attach_dynamo_sender_notes" {
    role       = "${aws_iam_role.send_note_update_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonDynamoDBReadOnlyAccess"
}

resource "aws_iam_role_policy_attachment" "attach_cloudwatch_sender_notes" {
    role       = "${aws_iam_role.send_note_update_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}

resource "aws_iam_role_policy_attachment" "attach_ses_sender_notes" {
    role       = "${aws_iam_role.send_note_update_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AmazonSESFullAccess"
}

resource "aws_iam_role_policy_attachment" "attach_stream_sender_notes" {
    role       = "${aws_iam_role.send_note_update_role.name}"
    policy_arn = "arn:aws:iam::aws:policy/AWSLambdaInvocation-DynamoDB"
}