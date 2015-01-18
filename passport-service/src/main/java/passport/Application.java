package passport;

import com.netflix.appinfo.InstanceInfo;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.FeignConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;
import java.util.List;

@SpringBootApplication
@EnableEurekaClient
public class Application extends FeignConfiguration {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    BookmarkClient bookmarkClient() {
        return loadBalance(BookmarkClient.class, "http://bookmark-service");
    }
}

@RestController
class Client {

    @Autowired
    private com.netflix.discovery.DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BookmarkClient bookmarkClient;

    // TODO NB: don't call this until about 30s after it's started up!!!
    // the load balancers need to refresh their list of servers otherwise they'll fail.
    @RequestMapping("/connect")
    public void connect() throws Exception {

        // get the info directly from the Eureka DiscoveryClient
        InstanceInfo photoServiceInstanceInfo = discoveryClient.getNextServerFromEureka(
                "photo-service", false);
        System.out.println("photoService: " + ToStringBuilder.reflectionToString(photoServiceInstanceInfo, ToStringStyle.MULTI_LINE_STYLE));

        InstanceInfo bookmarkServiceInstanceInfo = discoveryClient.getNextServerFromEureka(
                "bookmark-service", false);
        System.out.println("bookmarkService: " + ToStringBuilder.reflectionToString(
                bookmarkServiceInstanceInfo, ToStringStyle.MULTI_LINE_STYLE));

        InstanceInfo.InstanceStatus bookmarkStatus = bookmarkServiceInstanceInfo.getStatus();
        System.out.println("bookmark status: " + bookmarkStatus);

        InstanceInfo.InstanceStatus photoStatus = photoServiceInstanceInfo.getStatus();
        System.out.println("photo status: " + photoStatus);

        // use the "smart" Eureka-aware RestTemplate
        ResponseEntity<List<Bookmark>> exchange =
                this.restTemplate.exchange(
                        "http://bookmark-service/{userId}/bookmarks",
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<Bookmark>>() {
                        },
                        (Object) "mstine");

        enumerateBookmarks("mstine", exchange.getBody());

        enumerateBookmarks("jlong", bookmarkClient.getBookmarks("jlong"));
    }

    protected void enumerateBookmarks(String user, Collection<Bookmark> bookmark) {
        System.out.println ( "--------------------------------------") ;
        System.out.println("found " + bookmark.size() + " bookmarks for " + user + ".");
        bookmark.forEach(System.out::println);
    }

}


interface BookmarkClient {

    @RequestMapping(method = RequestMethod.GET, value = "/{userId}/bookmarks")
    List<Bookmark> getBookmarks(@PathVariable("userId") String userId);
}

class Bookmark {
    private Long id;
    private String href, label, description, userId;

    @Override
    public String toString() {
        return "Bookmark{" +
                "id=" + id +
                ", href='" + href + '\'' +
                ", label='" + label + '\'' +
                ", description='" + description + '\'' +
                ", userId='" + userId + '\'' +
                '}';
    }

    public Bookmark() {
    }

    public Long getId() {
        return id;
    }

    public String getHref() {
        return href;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getUserId() {
        return userId;
    }
}

