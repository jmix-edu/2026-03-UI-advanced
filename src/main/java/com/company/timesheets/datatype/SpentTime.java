package com.company.timesheets.datatype;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SpentTime implements Serializable {

    private static final BigDecimal MINUTES_IN_HOUR = BigDecimal.valueOf(60, 2);

    private final int hours;
    private final int minutes;

    public SpentTime(int hours, int minutes) {
        Preconditions.checkArgument(hours >= 0, "Hours must be positive");
        Preconditions.checkArgument(minutes >= 0 && minutes < 60, "Minutes must be between 0 and 59 inclusive");

        this.hours = hours;
        this.minutes = minutes;
    }

    public static SpentTime fromHours(int hours) {
        return new SpentTime(hours, 0);
    }

    public static SpentTime fromMinutes(long minutes) {
        int hours = 0;
        if (minutes >= 60) {
            hours = (int) (minutes / 60);
            minutes = minutes % 60;
        }
        return new SpentTime(hours, (int) minutes);
    }

    public int getHours() {
        return hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public long toMinutes() {
        return this.hours * 60L + this.minutes;
    }

    public BigDecimal asHours() {
        return BigDecimal.valueOf(toMinutes(), 2).divide(MINUTES_IN_HOUR, 2, RoundingMode.DOWN);
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d", hours, minutes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpentTime spentTime = (SpentTime) o;
        return hours == spentTime.hours && minutes == spentTime.minutes;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(hours, minutes);
    }
}
