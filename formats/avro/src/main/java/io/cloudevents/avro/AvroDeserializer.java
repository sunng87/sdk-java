/*
 * Copyright 2018-Present The CloudEvents Authors
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.cloudevents.avro;

import java.util.Map;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;

import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;
import io.cloudevents.avro.AvroCloudEvent;
import io.cloudevents.core.data.BytesCloudEventData;
import io.cloudevents.core.v1.CloudEventV1;
import io.cloudevents.rw.CloudEventRWException;
import io.cloudevents.rw.CloudEventReader;
import io.cloudevents.rw.CloudEventDataMapper;
import io.cloudevents.rw.CloudEventWriter;
import io.cloudevents.rw.CloudEventWriterFactory;

class AvroDeserializer implements CloudEventReader {

    private final AvroCloudEvent avroCloudEvent;

    public AvroDeserializer(AvroCloudEvent avroCloudEvent) {
        this.avroCloudEvent = avroCloudEvent;
    }

    @Override
    public <W extends CloudEventWriter<R>, R> R read(CloudEventWriterFactory<W, R> writerFactory,
                                                     CloudEventDataMapper<? extends CloudEventData> mapper) throws CloudEventRWException {
        Map<String, Object> avroCloudEventAttrs = this.avroCloudEvent.getAttribute();
        SpecVersion specVersion = SpecVersion.parse((String)avroCloudEventAttrs.get(CloudEventV1.SPECVERSION));
        final CloudEventWriter<R> writer = writerFactory.create(specVersion);

        for (Map.Entry<String, Object> entry: avroCloudEventAttrs.entrySet()) {
            String key = entry.getKey().toString();

            switch(key) {
                case CloudEventV1.SPECVERSION:
                    continue;
                case CloudEventV1.TIME: {
                    // OffsetDateTime
                    OffsetDateTime value = OffsetDateTime.parse((String) entry.getValue());
                    writer.withContextAttribute(key, value);
                };
                case CloudEventV1.DATASCHEMA: {
                    // URI
                    URI value = URI.create((String) entry.getValue());
                    writer.withContextAttribute(key, value);
                };
                default:
                    writer.withContextAttribute(key, (String) entry.getValue());
            }
        }

        ByteBuffer buffer = (ByteBuffer) this.avroCloudEvent.getData();

        if (buffer != null) {
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            return writer.end(mapper.map(BytesCloudEventData.wrap(data)));
        } else {
            return writer.end();
        }
    }

}