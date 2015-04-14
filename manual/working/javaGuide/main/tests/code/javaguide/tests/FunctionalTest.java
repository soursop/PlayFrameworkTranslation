package javaguide.tests;

import java.util.HashMap;
import java.util.Map;

import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.libs.F.*;
import play.libs.ws.*;

import static play.test.Helpers.*;
import static org.junit.Assert.*;

//#bad-route-import
import play.mvc.Http.RequestBuilder;
//#bad-route-import

//#test-withapp
public class FunctionalTest extends WithApplication {
//#test-withapp

    //#bad-route
    @Test
    public void testBadRoute() {
        RequestBuilder request = new RequestBuilder()
            .method(GET)
            .uri("/xx/Kiwi");

        Result result = route(request);
        assertEquals(NOT_FOUND, result.status());
    }
    //#bad-route

    int timeout = 5000;

    private TestServer testServer() {
        Map<String, String> config = new HashMap<String, String>();
        config.put("play.http.router", "javaguide.tests.Routes");
        return Helpers.testServer(fakeApplication(config));
    }

    private TestServer testServer(int port) {
        Map<String, String> config = new HashMap<String, String>();
        config.put("play.http.router", "javaguide.tests.Routes");
        return Helpers.testServer(port, fakeApplication(config));
    }

    //#test-server
    @Test
    public void testInServer() {
        running(testServer(3333), () -> {
            assertEquals(OK, WS.url("http://localhost:3333").get().get(timeout).getStatus());
        });
    }
    //#test-server

    //#test-browser
    @Test
    public void runInBrowser() {
        running(testServer(), HTMLUNIT, browser -> {
            browser.goTo("/");
            assertEquals("Welcome to Play!", browser.$("#title").getText());
            browser.$("a").click();
            assertEquals("/login", browser.url());
        });
    }
    //#test-browser
}
