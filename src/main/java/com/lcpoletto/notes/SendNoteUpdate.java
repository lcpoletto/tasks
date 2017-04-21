/**
 * 
 */
package com.lcpoletto.notes;

/**
 * AWS Lambda function that will react to a note update and send an e-mail to
 * the owner of the note if it was updated by another user.
 * 
 * This function will require the following policies:
 * arn:aws:iam::aws:policy/service-role/AWSLambdaDynamoDBExecutionRole
 * 
 * @author Luis Carlos Poletto
 *
 */
public class SendNoteUpdate {

}
