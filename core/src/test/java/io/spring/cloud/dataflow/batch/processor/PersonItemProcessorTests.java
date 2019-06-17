/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.cloud.dataflow.batch.processor;


import io.spring.cloud.dataflow.batch.domain.Person;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Test cases for PersonItemProcessor.
 *
 * @author Chris Schaefer
 */
public class PersonItemProcessorTests {
	private static final String FIRST_NAME = "jane";
	private static final String LAST_NAME = "doe";

	@Test
	public void testPersonProcessing() throws Exception {
		 Person person = new Person(0L, FIRST_NAME, LAST_NAME);

		PersonItemProcessor personItemProcessor = new PersonItemProcessor();
		personItemProcessor.setStringAction(PersonItemProcessor.Action.UPPERCASE.name());
		Person transformedPerson = personItemProcessor.process(person);

		assertNotNull("Received null Person", transformedPerson);
		assertNotNull("Received null first name", transformedPerson.getFirstName());
		assertNotNull("Received null last name", transformedPerson.getLastName());
		assertEquals("Invalid first name processing, should be uppercase",
					 person.getFirstName().toUpperCase(), transformedPerson.getFirstName());
		assertEquals("Invalid last name processing, should be uppercase",
					 person.getLastName().toUpperCase(), transformedPerson.getLastName());

		PersonItemProcessor personItemProcessor2 = new PersonItemProcessor();
		personItemProcessor2.setStringAction(PersonItemProcessor.Action.REVERSE.name());
		Person transformedPerson2 = personItemProcessor2.process(person);
		assertEquals("Invalid first name processing, should be backwards",
				new StringBuilder(person.getFirstName()).reverse().toString(), transformedPerson2.getFirstName());
		assertEquals("Invalid last name processing, should be backwards",
				new StringBuilder(person.getLastName()).reverse().toString(), transformedPerson2.getLastName());

		PersonItemProcessor personItemProcessor3 = new PersonItemProcessor();
		personItemProcessor3.setStringAction(PersonItemProcessor.Action.NONE.name());
		Person transformedPerson3 = personItemProcessor3.process(person);
		assertEquals("Invalid first name processing, should be same",
				person.getFirstName(), transformedPerson3.getFirstName());
		assertEquals("Invalid last name processing, should be same",
				person.getLastName(), transformedPerson3.getLastName());


	}


}
