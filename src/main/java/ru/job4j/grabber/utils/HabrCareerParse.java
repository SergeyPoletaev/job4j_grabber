package ru.job4j.grabber.utils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.Parse;
import ru.job4j.grabber.Post;
import ru.job4j.grabber.PsqlStore;
import ru.job4j.grabber.Store;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class HabrCareerParse implements Parse {
    private final HabrCareerDataTimeParser dataTimeParser;

    public HabrCareerParse(HabrCareerDataTimeParser dataTimeParser) {
        this.dataTimeParser = dataTimeParser;
    }

    public static void main(String[] args) {
        HabrCareerParse careerParse = new HabrCareerParse(new HabrCareerDataTimeParser());
        String link = "https://career.habr.com/vacancies/java_developer";
        List<Post> rslPosts = careerParse.list(link);
        System.out.println("Список полученных вакансий c сайта:");
        rslPosts.forEach(System.out::println);
        Store store = new PsqlStore(careerParse.getProperties());
        rslPosts.forEach(store::save);
        String ln = System.lineSeparator();
        System.out.println(ln + "Получаем все вакансии из базы данных:");
        List<Post> postsFromDb = store.getAll();
        postsFromDb.forEach(System.out::println);
        System.out.println(ln + "Получаем вакансию по id = 3:");
        Post postFromDb = store.findById(3);
        System.out.println(postFromDb);
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream in = HabrCareerParse.class.getClassLoader().getResourceAsStream("rabbit.properties")) {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
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
                rows.forEach(row -> posts.add(getPost(row, link)));
                rslPosts.addAll(posts);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rslPosts;
    }

    private Post getPost(Element row, String link) {
        Element titleElement = row.select(".vacancy-card__title").first();
        Element linkElement = titleElement.child(0);
        String vacancyName = titleElement.text();
        Element dateElement = row.select(".vacancy-card__date").first();
        Element vacancyDate = dateElement.child(0);
        String sourceLink = link.substring(0, 23);
        String vacancyLink = String.format("%s%s", sourceLink, linkElement.attr("href"));
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
