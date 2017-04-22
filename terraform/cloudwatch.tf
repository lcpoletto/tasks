resource "aws_cloudwatch_event_rule" "send_tasks_daily_cw_rule" {
	name = "send_tasks_daily"
	description = "Send uncompleted tasks daily to users."
	schedule_expression = "rate(1 day)"
}

resource "aws_cloudwatch_event_target" "send_tasks_daily_cw_target" {
	rule = "${aws_cloudwatch_event_rule.send_tasks_daily_cw_rule.name}"
	arn = "${aws_lambda_function.send_task_lambda.arn}"
	input = <<EOF
""
EOF
}