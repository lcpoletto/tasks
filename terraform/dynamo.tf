resource "aws_dynamodb_table" "tasks_table" {
    name = "tasks"
    read_capacity = "5"
    write_capacity = "5"
    hash_key = "id"
    
    attribute {
    	name = "id"
    	type = "S"
    }
}

# below table is for part 2
resource "aws_dynamodb_table" "notes_table" {
    name = "notes"
    read_capacity = "5"
    write_capacity = "5"
    hash_key = "id"
    stream_enabled = true
    stream_view_type = "NEW_IMAGE"
    
    attribute {
    	name = "id"
    	type = "S"
    }
}