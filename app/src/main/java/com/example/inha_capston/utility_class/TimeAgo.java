package com.example.inha_capston.utility_class;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * calculate time passed from current time
 */
public class TimeAgo {

    /**
     * function for return string
     * @param duration
     * @return string from time passed
     */
    public String getTimeAgo(long duration) {
        Date now = new Date();

        // calculate created time
        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - duration);
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - duration);
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - duration);

        if(seconds < 60)
            return "방금";
        else if(minutes >= 1 && minutes < 60)
            return minutes + "분 전";
        else if(hours >= 1 && hours < 24)
            return hours + "시간 전";
        else
            return days + "일 전";
    }
}
