package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class HabrCareerDataTimeParser implements JavaTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        ZonedDateTime dateTime = ZonedDateTime.parse(parse);
        return dateTime.toLocalDateTime();
    }
}
