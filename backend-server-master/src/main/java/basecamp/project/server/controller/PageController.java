package basecamp.project.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


/**
 * Setzt das Controller Mapping (Seiten-Navigation) für das springframework.
 */
@Controller
public class PageController {
    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
    public String index() {
        return "index";
    }

    @RequestMapping(value = {"/about"}, method = RequestMethod.GET)
    public String about() {
        return "about";
    }
}