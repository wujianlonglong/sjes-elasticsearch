package sjes.elasticsearch.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import sjes.elasticsearch.utils.DateConvertUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by qinhailong on 15-11-20.
 */
public class CustomDateSerializer extends StdScalarSerializer<LocalDateTime> {

    public CustomDateSerializer() {
        super(LocalDateTime.class);
    }

    /**
     * Method that can be called to ask implementation to serialize
     * values of type this serializer handles.
     *
     * @param value       Value to serialize; can <b>not</b> be null.
     * @param gen         Generator used to output resulting Json content
     * @param serializers Provider that can be used to get serializers for
     */
    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        Date date = DateConvertUtils.asUtilDate(value);
        if (null != date) {
            gen.writeNumber(date.getTime());
        }
    }
}
