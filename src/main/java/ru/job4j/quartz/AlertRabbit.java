package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            SimpleScheduleBuilder items = simpleSchedule()
                    .withIntervalInSeconds(getInterval())
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(items)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

    private static int getInterval() {
        int interval = 0;
        String propFile = "rabbit.properties";
        String key = "rabbit.interval";
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream(propFile)) {
            Properties properties = new Properties();
            properties.load(in);
            interval = Integer.parseInt(properties.getProperty(key));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(
                    String.format("Invalid value for key %s in file %s. The value must be a number", key, propFile)
            );
        }
        return interval;
    }

    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            System.out.println("Rabbit runs here ...");
        }
    }
}
