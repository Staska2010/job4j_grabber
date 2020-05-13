package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AllertRabbit {
    private static final Logger LOG = LoggerFactory.getLogger(AllertRabbit.class.getName());
    private static Properties config = new Properties();
    Connection con;

    private void loadProps() {
        try (InputStream is = AllertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(is);
        } catch (IOException exc) {
            LOG.error("Properties file reading fault", exc);
        }
    }

    public void run() {
        loadProps();
        try {
            String driver = config.getProperty("driver");
            Class.forName(driver);
            con = DriverManager.getConnection(
                    config.getProperty("database_url"),
                    config.getProperty("user"),
                    config.getProperty("password"));
            JobDataMap data = new JobDataMap();
            data.put("connection", con);
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).usingJobData(data).build();
            SimpleScheduleBuilder timetable = simpleSchedule()
                    .withIntervalInSeconds(5)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(timetable)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception exc) {
            LOG.error("Exception", exc);
        }
    }

    public static void main(String[] args) {
        new AllertRabbit().run();
    }

    public static class Rabbit implements Job {
        public Rabbit() {
            System.out.println(hashCode());
        }
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("Rabbit runs here...");
            String query = "INSERT INTO rabbit (created_date) VALUES (?);";
            Connection con = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
                ps.executeUpdate();
            } catch (SQLException exc) {
                LOG.error("Rabbit fault", exc);
            }
        }
    }
}
