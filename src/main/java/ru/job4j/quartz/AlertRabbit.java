package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder items = simpleSchedule()
                    .withIntervalInSeconds(getInterval())
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(items)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SQLException | InterruptedException | SchedulerException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() {
        Connection connect;
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            Class.forName(properties.getProperty("driver-class-name"));
            connect = DriverManager.getConnection(
                    properties.getProperty("url"),
                    properties.getProperty("username"),
                    properties.getProperty("password"));
        } catch (Exception e) {
            throw new IllegalStateException();
        }
        return connect;
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

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) jobExecutionContext.getJobDetail().getJobDataMap().get("connection");
            add(connection, System.currentTimeMillis());
        }

        private void add(Connection connection, long currentTime) {
            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "insert into rabbit (created_data) values (?)")) {
                statement.setTimestamp(1, new Timestamp(currentTime));
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
