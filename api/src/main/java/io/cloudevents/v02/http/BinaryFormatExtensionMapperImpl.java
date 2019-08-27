/**
 * Copyright 2019 The CloudEvents Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.cloudevents.v02.http;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.AbstractMap.SimpleEntry;
import java.util.stream.Collectors;

import io.cloudevents.fun.BinaryFormatExtensionMapper;
import io.cloudevents.v02.ContextAttributes;

/**
 * 
 * @author fabiojose
 *
 */
public class BinaryFormatExtensionMapperImpl implements
	BinaryFormatExtensionMapper {
	
	private static final List<String> RESERVED_HEADERS = 
		ContextAttributes.VALUES.stream()
			.map(attribute -> BinaryFormatAttributeMapperImpl
					.HEADER_PREFIX + attribute)
			.collect(Collectors.toList());
	static {
		RESERVED_HEADERS.add("content-type");
	};

	@Override
	public Map<String, String> map(Map<String, Object> headers) {
		Objects.requireNonNull(headers);
		
		// remove all reserved words and then remaining may be extensions
		return 
		headers.entrySet()
			.stream()
			.map(header -> new SimpleEntry<>(header.getKey()
					.toLowerCase(Locale.US), header.getValue().toString()))
			.filter(header -> {
				return !RESERVED_HEADERS.contains(header.getKey());
			})
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}

}
