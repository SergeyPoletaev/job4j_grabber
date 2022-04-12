package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AlertRabbit {

    public static void main(String[] args) {
        Properties properties = getProperties();
        try (Connection connection = getConnection(properties)) {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder items = simpleSchedule()
                    .withIntervalInSeconds(Integer.parseInt(properties.getProperty("rabbit.interval")))
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(items)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (SQLException | InterruptedException | SchedulerException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream in = AlertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        } catch (Exception e) {
            throw new IllegalStateException();
        }
        return properties;
    }

    private static Connection getConnection(Properties properties) throws ClassNotFoundException, SQLException {
        Class.forName(properties.getProperty("driver-class-name"));
        return DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("username"),
                properties.getProperty("password"));
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) jobExecutionContext.getJobDetail().getJobDataMap().get("connection");
            add(connection);
        }

        private void add(Connection connection) {
            try (PreparedStatement statement =
                         connection.prepareStatement(
                                 "insert into rabbit (created_data) values (?)")) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                statement.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
