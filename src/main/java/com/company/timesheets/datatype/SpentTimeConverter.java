package com.company.timesheets.datatype;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.lang.Nullable;

@Converter(autoApply = true)
public class SpentTimeConverter implements AttributeConverter<SpentTime, Long> {

    @Nullable
    @Override
    public Long convertToDatabaseColumn(@Nullable SpentTime attribute) {
        return attribute != null
                ? attribute.toMinutes()
                : null;
    }

    @Nullable
    @Override
    public SpentTime convertToEntityAttribute(@Nullable Long dbData) {
        return dbData != null
                ? SpentTime.fromMinutes(dbData)
                : null;
    }
}
