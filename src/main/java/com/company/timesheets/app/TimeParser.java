package com.company.timesheets.app;

import com.company.timesheets.datatype.SpentTime;
import io.jmix.core.Messages;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component("ts_TimeParser")
public class TimeParser {

    private final Messages messages;

    public TimeParser(Messages messages) {
        this.messages = messages;
    }

    public SpentTime parse(String time) throws ParseException {

        if (time.matches("\\d+[: ]\\d{1,2}")) {
            String[] parts = time.split("[: ]");
            if (parts.length == 2) {
                return new SpentTime(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }

            throw new ParseException("Cannot parse time string: " + time, 0);
        }

        if (StringUtils.isNumeric(time)) {
            int numericTime = Math.abs(Integer.parseInt(time));
            long hoursInDay = TimeUnit.DAYS.toHours(1);
            if (numericTime <= hoursInDay) {
                return SpentTime.fromHours(numericTime);
            } else {
                return SpentTime.fromMinutes(numericTime);
            }
        }

        int hours = findHours(time);
        int minutes = findMinutes(time);

        if (hours >= 0 || minutes >= 0) {
            return new SpentTime(Math.max(0, hours), Math.max(0, minutes));
        }

        throw new ParseException("Cannot parse time string: " + time, 0);
    }

    private int findHours(String time) {
        return findTimeValue(time, messages.getMessage("datatype.spentTime.hours.units"));
    }

    private int findMinutes(String time) {
        return findTimeValue(time, messages.getMessage("datatype.spentTime.minutes.units"));
    }

    private int findTimeValue(String time, String units) {
        String regex = "-?\\d+\\s*(" + units + ")";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(time);
        if (matcher.find()) {
            String value = matcher.group();
            regex = "-?\\d+";
            pattern = Pattern.compile(regex);
            matcher = pattern.matcher(value);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }
        }

        return -1;
    }
}