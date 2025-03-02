package com.myparty.app.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class TwilioService {

	@Value("${twilio.account.sid}")
	private String accountSid;

	@Value("${twilio.auth.token}")
	private String authToken;

	@Value("${twilio.phone.number}")
	private String twilioPhoneNumber;

	public boolean sendSMS(String to, String message) {
		Twilio.init(accountSid, authToken);

		Message sms = Message.creator(
				new PhoneNumber(to),
				new PhoneNumber(twilioPhoneNumber),
				message
		).create();

		return sms.getStatus() != Message.Status.FAILED;
	}
}
