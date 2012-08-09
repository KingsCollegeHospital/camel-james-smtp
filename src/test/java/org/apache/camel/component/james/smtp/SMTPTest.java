/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.camel.component.james.smtp;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;

import javax.mail.Header;
import javax.mail.internet.MimeMessage;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.commons.net.smtp.SMTPClient;
import org.junit.Test;

// TODO: Auto-generated Javadoc
/**
 * The Class SMTPTest.
 */
public class SMTPTest extends CamelTestSupport {

	/** The result endpoint. */
	@EndpointInject(uri = "mock:result")
	protected MockEndpoint resultEndpoint;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.camel.test.junit4.CamelTestSupport#createRouteBuilder()
	 */
	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() {
				// from("file://d:/tmp?noop=true").to("mock:result");

				from("james-smtp:localhost:2525").to("mock:result");
			}
		};
	}

	/**
	 * Test send matching message.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testSendMatchingMessage() throws Exception {
		String sender = "sender@localhost";
		String rcpt = "rcpt@localhost";
		String body = "Subject: test\r\n\r\nTestmail";
		SMTPClient client = new SMTPClient();
		client.connect("localhost", 2525);
		client.helo("localhost");
		client.setSender(sender);
		client.addRecipient(rcpt);

		client.sendShortMessageData(body);
		client.quit();
		client.disconnect();
		resultEndpoint.expectedMessageCount(1);
		resultEndpoint.expectedBodyReceived().body(InputStream.class);
		Exchange ex = resultEndpoint.getReceivedExchanges().get(0);
		Map<String, Object> headers = ex.getIn().getHeaders();
		assertEquals(sender,
				headers.get(MailEnvelopeMessage.SMTP_SENDER_ADRRESS));
		assertEquals(rcpt,
				headers.get(MailEnvelopeMessage.SMTP_RCPT_ADRRESS_LIST));

		// check type converter
		MimeMessage message = ex.getIn().getBody(MimeMessage.class);
		Enumeration<Header> mHeaders = message.getAllHeaders();
		Header header = null;
		while (mHeaders.hasMoreElements()) {
			header = mHeaders.nextElement();
			if (header.getName().equals("Subject")) {
				break;
			}
		}
		assertNotNull(header);
		assertEquals("Subject", header.getName());
		assertEquals(header.getValue(), "test");

		resultEndpoint.assertIsSatisfied();
	}

}
