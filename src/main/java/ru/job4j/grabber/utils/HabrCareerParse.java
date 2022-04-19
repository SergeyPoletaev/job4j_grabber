package ru.job4j.grabber.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Parse;
import ru.job4j.grabber.Post;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);
    private final HabrCareerDataTimeParser dataTimeParser;

    public HabrCareerParse(HabrCareerDataTimeParser dataTimeParser) {
        this.dataTimeParser = dataTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse careerParse = new HabrCareerParse(new HabrCareerDataTimeParser());
        List<Post> rslPosts = careerParse.list(PAGE_LINK);
        rslPosts.forEach(System.out::println);
    }

    private String retrieveDescription(String link) throws IOException {
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Element row = document.select(".style-ugc").first();
        return row.text();
    }

    @Override
    public List<Post> list(String link) {
        List<Post> rslPosts = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            try {
                Connection connection = Jsoup.connect(link + "?page=" + i);
                Document document = connection.get();
                List<Post> posts = new ArrayList<>();
                Elements rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> posts.add(getPost(row)));
                rslPosts.addAll(posts);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rslPosts;
    }

    private Post getPost(Element row) {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        Element dateElement = row.select(".vacancy-card__date").first();
        Element vacancyDate = dateElement.child(0);
        String vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
        LocalDateTime vacancyCreated = dataTimeParser.parse(vacancyDate.attr("datetime"));
        String vacancyDescription = "";
        try {
            vacancyDescription = retrieveDescription(vacancyLink);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Post(vacancyName, vacancyLink, vacancyDescription, vacancyCreated);
    }
}
