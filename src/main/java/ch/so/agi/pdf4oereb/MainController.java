package ch.so.agi.pdf4oereb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/ping")
    public ResponseEntity<String>  ping() {
        log.info("pdf4oereb-web-service");
        return new ResponseEntity<String>("pdf4oereb-web-service",HttpStatus.OK);
    }
    
    
}
