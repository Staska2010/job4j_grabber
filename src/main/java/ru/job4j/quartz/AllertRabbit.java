package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class AllertRabbit {
    private static final Logger LOG = LoggerFactory.getLogger(AllertRabbit.class.getName());
    private static Properties config = new Properties();

    private static void init() {
        try (InputStream is = AllertRabbit.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            config.load(is);
        } catch (IOException exc) {
            LOG.error("File not found", exc);
        }
    }

    public static void main(String[] args) {
        init();
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class).build();
            int interval = Integer.parseInt(config.getProperty("rabbit.interval", "10"));
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException exc) {
           // LOG.error(exc.toString());
        }
    }

    public static class Rabbit implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            System.out.println("Rabbit runs here...");
        }
    }
}
