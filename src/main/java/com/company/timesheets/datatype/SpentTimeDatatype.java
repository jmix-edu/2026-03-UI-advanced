package com.company.timesheets.datatype;

import com.company.timesheets.app.TimeParser;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.annotation.DatatypeDef;
import io.jmix.core.metamodel.annotation.Ddl;
import io.jmix.core.metamodel.datatype.Datatype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.text.ParseException;
import java.util.Locale;

@DatatypeDef(
        id = "spentTime",
        javaClass = SpentTime.class,
        defaultForClass = true
)
@Ddl("BIGINT")
public class SpentTimeDatatype implements Datatype<SpentTime> {

    @Autowired
    private TimeParser timeParser;
    @Autowired
    private Messages messages;

    @Override
    public String format(@Nullable Object value) {
        return value instanceof SpentTime spentTime
                ? messages.formatMessage("", "datatype.spentTime.format", spentTime.getHours(), spentTime.getMinutes())
                : "";
    }

    @Override
    public String format(@Nullable Object value, Locale locale) {
        return format(value);
    }

    @Nullable
    @Override
    public SpentTime parse(@Nullable String value) throws ParseException {
        if (value == null) {
            return null;
        }

        return timeParser.parse(value);
    }

    @Nullable
    @Override
    public SpentTime parse(@Nullable String value, Locale locale) throws ParseException {
        return parse(value);
    }
}
