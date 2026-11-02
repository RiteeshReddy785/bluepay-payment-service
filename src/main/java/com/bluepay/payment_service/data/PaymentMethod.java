package com.bluepay.payment_service.data;


/*
 * why enum is importatnt is if it is string like this private String status;
 * 
 * payment.setStatus("SUCCESS");
payment.setStatus("success");
payment.setStatus("SUCESS");
payment.setStatus("HELLO"); He may define multiple things which might not valid if it is 
enum we can set like this payment.setStatus(PaymentStatus.PENDING);
 * 
 * 
 * 
 * 
 * 
 */
public enum PaymentMethod {

	
	  CARD,
	    BANK_TRANSFER,
	    UPI,
	    WALLET
}
