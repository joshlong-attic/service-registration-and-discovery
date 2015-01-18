package bookmarks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SpringBootApplication
@EnableEurekaClient
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    CommandLineRunner init(BookmarkRepository bookmarkRepository) {
        return args -> {
            bookmarkRepository.deleteAll();

            Arrays.asList("mstine", "jlong").forEach(n ->
                    bookmarkRepository.save(new Bookmark(n,
                            "http://some-other-host" + n + ".com/",
                            "A description for " + n + "'s link",
                            n)));
        };
    }
}

@RestController
@RequestMapping("/{userId}/bookmarks")
class BookmarkRestController {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @RequestMapping(method = RequestMethod.GET)
    Collection<Bookmark> getBookmarks(@PathVariable String userId) {
        return this.bookmarkRepository.findByUserId(userId);
    }

    @RequestMapping(value = "/{bookmarkId}", method = RequestMethod.GET)
    Bookmark getBookmark(@PathVariable String userId,
                         @PathVariable Long bookmarkId) {
        return this.bookmarkRepository.findByUserIdAndId(userId, bookmarkId);
    }

    @RequestMapping(method = RequestMethod.POST)
    Bookmark createBookmark(@PathVariable String userId,
                            @RequestBody Bookmark bookmark) {

        Bookmark bookmarkInstance = new Bookmark(
                userId,
                bookmark.getHref(),
                bookmark.getDescription(),
                bookmark.getLabel());

        return this.bookmarkRepository.save(bookmarkInstance);
    }

}


interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Bookmark findByUserIdAndId(String userId, Long id);

    List<Bookmark> findByUserId(String userId);
}

@Entity
class Bookmark {

    private String userId;

    @Id
    @GeneratedValue
    private Long id;

    private String href;

    private String description;

    Bookmark() {
    }

    public Bookmark(String userId, String href,
                    String description, String label) {
        this.userId = userId;
        this.href = href;
        this.description = description;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getUserId() {
        return userId;
    }

    public Long getId() {
        return id;
    }

    public String getHref() {
        return href;
    }

    public String getDescription() {
        return description;
    }

    private String label;
}