package basecamp.project.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Setzt das Controller Mapping (Seiten-Navigation) für das springframework.
 */

@Controller
public class IndexController {
    @RequestMapping(value = {"/", "/index"}, method = RequestMethod.GET)
    public String index(Model model) {
        return "index";
    }

    @RequestMapping(value = {"/graph"}, method = RequestMethod.GET)
    public String graph(Model model) {
        return "graph";
    }

    @RequestMapping(value = {"/about"}, method = RequestMethod.GET)
    public String about() {
        return "about";
    }
}
