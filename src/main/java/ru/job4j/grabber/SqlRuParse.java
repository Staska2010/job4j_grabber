package ru.job4j.grabber;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;

public class SqlRuParse implements Parse {
    private static final Logger LOG = LoggerFactory.getLogger(SqlRuParse.class);

    private LocalDate parseDate(String dateTime) {
        DateTimeFormatter format;
        LocalDate result;
        String date = dateTime.split(",")[0];
        Map<Long, String> months = new HashMap<>();
        months.put(1L, "янв");
        months.put(2L, "фев");
        months.put(3L, "мар");
        months.put(4L, "апр");
        months.put(5L, "май");
        months.put(6L, "июн");
        months.put(7L, "июл");
        months.put(8L, "авг");
        months.put(9L, "сен");
        months.put(10L, "окт");
        months.put(11L, "ноя");
        months.put(12L, "дек");
        format = new DateTimeFormatterBuilder()
                .appendPattern("d ")
                .appendText(ChronoField.MONTH_OF_YEAR, months)
                .appendPattern(" yy")
                .toFormatter(new Locale("ru"));
        if (date.startsWith("сегодня")) {
            result = LocalDate.now();
        } else if (date.startsWith("вчера")) {
            result = LocalDate.now().minusDays(1);
        } else {
            result = LocalDate.parse(date, format);
        }
        return result;
    }

    private boolean isVacancyRow(Element cell) {
        return !cell.text().startsWith("Важно");
    }

    private String getJobDetails(Element link) {
        String postDetails = "";
        try {
            Document jobDetailsPage = Jsoup.connect(link.attr("href")).get();
            Element details = jobDetailsPage.getElementsByClass("msgBody").get(1);
            Whitelist whitelist = new Whitelist();
            whitelist.addTags("br");
            postDetails = Jsoup.clean(details.html(), whitelist);
            postDetails = postDetails.replaceAll("<br>", "");
            postDetails = postDetails.replaceAll("\n\\s+", "\n");
        } catch (IOException exc) {
            LOG.error("Connection fault", exc);
        }
        return postDetails;
    }

    @Override
    public List<Post> list(String link) {
        List<Post> result = new LinkedList<>();
        LocalDate localDate = LocalDate.now();
        Document doc;
        for (int i = 1; i <= 5; i++) {
            try {
                doc = Jsoup.connect(link + "/" + i).get();
                Elements row = doc.select(".postslisttopic");
                for (Element td : row) {
                    if (isVacancyRow(td)) {
                        Post newPost = new Post();
                        newPost.id = 0;
                        Element href = td.child(0);
                        newPost.link = href.attr("href");
                        newPost.name = href.text();
                        newPost.description = getJobDetails(href);
                        Element next = td.lastElementSibling();
                        String dateTime = next.text();
                        localDate = parseDate(dateTime);
                        newPost.date = localDate;
                        result.add(newPost);
                    }
                }
            } catch (IOException exc) {
                LOG.error("Connection fault", exc);
            }
        }
        return result;
    }

    @Override
    public Post detail(String link) {

        return null;
    }
}
