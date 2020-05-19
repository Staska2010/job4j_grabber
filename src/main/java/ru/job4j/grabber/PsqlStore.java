package ru.job4j.grabber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(PsqlStore.class);

    private Connection con;

    public PsqlStore(Properties config) {
        try {
            Class.forName(config.getProperty("driver"));
            con = DriverManager.getConnection(
                    config.getProperty("database_url"),
                    config.getProperty("user"),
                    config.getProperty("password"));
        } catch (ClassNotFoundException exc) {
            LOG.error("Driver load fault", exc);
        } catch (SQLException exc) {
            LOG.error("DB connection fault", exc);
        }
    }

    @Override
    public void save(Post post) {
        String query = "INSERT INTO post (name, text, link, created) VALUES (?, ?, ?, ?);";
        try (PreparedStatement st = con.prepareStatement(query)) {
            st.setString(1, post.getName());
            st.setString(2, post.getDescription());
            st.setString(3, post.getLink());
            st.setDate(4, Date.valueOf(post.getDate()));
            st.executeUpdate();
        } catch (SQLException exc) {
            LOG.error("Query fault", exc);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> allPosts = new LinkedList<>();
        String query = "SELECT * FROM post";
        try (PreparedStatement pst = con.prepareStatement(query)) {
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                Post newPost = new Post();
                newPost.setId(rs.getInt("id"));
                newPost.setName(rs.getString("name"));
                newPost.setLink(rs.getString("link"));
                newPost.setDescription(rs.getString("text"));
                newPost.setDate(rs.getDate("created").toLocalDate());
                allPosts.add(newPost);
            }
        } catch (SQLException exc) {
            LOG.error("Query fault", exc);
        }
        return allPosts;
    }

    @Override
    public Post findById(String id) {
        String query = "SELECT * FROM post WHERE id = ?;";
        Post newPost = new Post();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, Integer.parseInt(id));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                newPost.setId(rs.getInt("id"));
                newPost.setName(rs.getString("name"));
                newPost.setLink(rs.getString("link"));
                newPost.setDescription(rs.getString("text"));
                newPost.setDate(rs.getDate("created").toLocalDate());
            }
        } catch (SQLException exc) {
            LOG.error("Query fault", exc);
        }
        return newPost;
    }

    @Override
    public void close() throws Exception {
        if (con != null) {
            con.close();
        }
    }

    public static void main(String[] args) throws Exception {
        Properties config = new Properties();
        Path path = Path.of("./src/main/resources/rabbit.properties");
        config.load(Files.newInputStream(path));
        PsqlStore ps = new PsqlStore(config);
        Post newPost = new Post();
        newPost.setName("name");
        newPost.setLink("https://www.sql.ru/forum/1325406/veb-razrabotchik4");
        newPost.setDescription("text");
        newPost.setDate(LocalDate.now());
        ps.save(newPost);
        ps.findById("1");
        newPost = new Post();
        newPost.setName("name2");
        newPost.setLink("https://www.sql.ru/forum/1325406/veb-razrabotchik5");
        newPost.setDescription("text2");
        newPost.setDate(LocalDate.now());
        ps.save(newPost);
        List<Post> get = ps.getAll();
    }
}
