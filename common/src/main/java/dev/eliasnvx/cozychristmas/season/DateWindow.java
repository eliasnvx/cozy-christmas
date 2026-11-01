package dev.eliasnvx.cozychristmas.season;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A yearly window of calendar days, both ends included. It may wrap over the new year ({@code 12-01} to {@code 01-07}).
 * Written as {@code "MM-dd"} in the config.
 */
public record DateWindow(MonthDay start, MonthDay end) {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("MM-dd");

    public static final Codec<MonthDay> MONTH_DAY_CODEC = Codec.STRING.comapFlatMap(DateWindow::parse, FORMAT::format);

    public static final Codec<DateWindow> CODEC = RecordCodecBuilder.create(i -> i.group(
            MONTH_DAY_CODEC.fieldOf("start").forGetter(DateWindow::start),
            MONTH_DAY_CODEC.fieldOf("end").forGetter(DateWindow::end)
    ).apply(i, DateWindow::new));

    public static DateWindow of(int startMonth, int startDay, int endMonth, int endDay) {
        return new DateWindow(MonthDay.of(startMonth, startDay), MonthDay.of(endMonth, endDay));
    }

    public boolean contains(MonthDay day) {
        if (!start.isAfter(end)) {
            return !day.isBefore(start) && !day.isAfter(end);
        }
        return !day.isBefore(start) || !day.isAfter(end); // wraps over the new year
    }

    private static DataResult<MonthDay> parse(String text) {
        try {
            return DataResult.success(MonthDay.parse(text, FORMAT));
        } catch (DateTimeParseException e) {
            return DataResult.error(() -> "Not a MM-dd date: " + text);
        }
    }
}
