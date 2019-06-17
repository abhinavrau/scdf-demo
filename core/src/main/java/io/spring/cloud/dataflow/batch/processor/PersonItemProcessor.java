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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.spring.cloud.dataflow.batch.domain.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.*;

/**
 * Processes the providing record, transforming the data into
 * uppercase characters.
 *
 * @author Chris Schaefer
 */

public class PersonItemProcessor implements ItemProcessor<Person, Person> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PersonItemProcessor.class);



	private String stringAction = "NONE";


	public enum Action
	{
		NONE,
		UPPERCASE,
		LOWERCASE,
		REVERSE;

		public static Set<String> strValues() {
			Set<String> ret = Sets.newHashSet();
			for (Action action : Action.values()) {
				ret.add(action.toString());
			}
			return ret;
		}
	}



	public void setStringAction(String action)
	{
		if(Action.strValues().contains(action)) {
			this.stringAction = action;
		}
	}

	@Override
	public Person process(Person person) throws Exception {

		String firstName = person.getFirstName();
		String lastName = person.getLastName();

		if(Strings.isNullOrEmpty(this.stringAction))
		{
			throw new RuntimeException("Action is null or empty. Please pass valid actions: " + Action.values());
		}

		LOGGER.info("Action is: " + this.stringAction);

		Action action = Action.valueOf(stringAction);
		switch (action)
		{

			case NONE:
				  break;
			case UPPERCASE:
				if(firstName.equalsIgnoreCase("Thanos"))
				{
					throw new RuntimeException("Danger!! - Thanos is not allowed");
				}
				firstName = firstName.toUpperCase();
				lastName = lastName.toUpperCase();
						break;

			case LOWERCASE:
				firstName = firstLetterCaps(firstName);
				lastName = firstLetterCaps(lastName);
						break;
			case REVERSE:
				if(firstName.equalsIgnoreCase("Paul") && lastName.equalsIgnoreCase("McCartney"))
				{
					throw new RuntimeException("Paul McCartney is not dead :-)");
				}
				firstName =  new StringBuilder(firstName).reverse().toString();
				lastName = new StringBuilder(lastName).reverse().toString();
						break;
		}

		Person processedPerson = new Person(person.getId(), firstName, lastName);

		LOGGER.info("Processed: " + person + " into: " + processedPerson);

		return processedPerson;
	}

	static public String firstLetterCaps ( String data )
	{
		String firstLetter = data.substring(0,1).toUpperCase();
		String restLetters = data.substring(1).toLowerCase();
		return firstLetter + restLetters;
	}
}
