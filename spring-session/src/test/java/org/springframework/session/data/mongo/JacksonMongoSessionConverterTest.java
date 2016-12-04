/*
 * Copyright 2014-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.session.data.mongo;

import com.mongodb.DBObject;
import org.junit.Test;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.SavedRequest;

import javax.servlet.ServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jakub Kubrynski
 */
public class JacksonMongoSessionConverterTest {

	JacksonMongoSessionConverter sut = new JacksonMongoSessionConverter();

	@Test
	public void shouldSaveIdField() throws Exception {
		//given
		MongoExpiringSession session = new MongoExpiringSession();

		//when
		DBObject convert = this.sut.convert(session);

		//then
		assertThat(convert.get("_id")).isEqualTo(session.getId());
		assertThat(convert.get("id")).isNull();
	}

	@Test
	public void shouldSerializeAllBasicFields() throws Exception {
		//given
		MongoExpiringSession session = new MongoExpiringSession();

		//when
		DBObject convert = this.sut.convert(session);

		//then
		assertThat(convert.get("created")).isEqualTo(session.getCreationTime());
		assertThat(convert.get("accessed")).isEqualTo(session.getLastAccessedTime());
		assertThat(convert.get("interval")).isEqualTo(session.getMaxInactiveIntervalInSeconds());
		assertThat(convert.get("expireAt")).isEqualTo(session.getExpireAt());
	}

	@Test
	public void shouldDeserializeAllBasicFields() throws Exception {
		//given
		MongoExpiringSession session = new MongoExpiringSession();

		//when
		DBObject json = this.sut.convert(session);

		MongoExpiringSession deserializedSession = this.sut.convert(json);

		//then
		assertThat(deserializedSession.getCreationTime()).isEqualTo(session.getCreationTime());
		assertThat(deserializedSession.getLastAccessedTime()).isEqualTo(session.getLastAccessedTime());
		assertThat(deserializedSession.getMaxInactiveIntervalInSeconds()).isEqualTo(session.getMaxInactiveIntervalInSeconds());
		assertThat(deserializedSession.getExpireAt()).isEqualTo(session.getExpireAt());
	}

	@Test
	public void shouldQueryAgainstAttribute() throws Exception {
		//when
		Query cart = this.sut.getQueryForIndex("cart", "my-cart");

		//then
		assertThat(cart.getQueryObject().get("attrs.cart")).isEqualTo("my-cart");
	}

	@Test
	public void shouldSerializeSavedRequest() throws Exception {
		//given
		MongoExpiringSession session = new MongoExpiringSession();

		final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
		PortResolver portResolver = new PortResolver() {
			public int getServerPort(ServletRequest request) {
				return 8080;
			}
		};

		DefaultSavedRequest savedRequest = new DefaultSavedRequest(request, portResolver);

		//when
		session.setAttribute(SAVED_REQUEST, savedRequest);
		DBObject json = this.sut.convert(session);

		//then
		assertThat(json.get("attrs")).isNotNull();
	}

	@Test
	public void shouldDeserializeSavedRequest() throws Exception {
		//given
		MongoExpiringSession session = new MongoExpiringSession();

		final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";

		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/test");
		PortResolver portResolver = new PortResolver() {
			public int getServerPort(ServletRequest request) {
				return 8080;
			}
		};

		DefaultSavedRequest savedRequest = new DefaultSavedRequest(request, portResolver);

		//when
		session.setAttribute(SAVED_REQUEST, savedRequest);
		DBObject json = this.sut.convert(session);
		MongoExpiringSession deserializedSession = this.sut.convert(json);

		//then
		assertThat(deserializedSession).isNotNull();
		assertThat(deserializedSession.getAttribute(SAVED_REQUEST)).isInstanceOf(SavedRequest.class);
	}
}
