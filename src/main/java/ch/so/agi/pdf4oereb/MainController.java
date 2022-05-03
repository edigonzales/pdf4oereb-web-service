package ch.so.agi.pdf4oereb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import ch.so.agi.oereb.pdf4oereb.Converter;
import ch.so.agi.oereb.pdf4oereb.ConverterException;
import ch.so.agi.oereb.pdf4oereb.Locale;

@Controller
public class MainController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ServletContext servletContext;

    private static String FOLDER_PREFIX = "pdf4oereb_";

    @GetMapping("/ping")
    public ResponseEntity<String>  ping() {
        log.info("pdf4oereb-web-service");
        return new ResponseEntity<String>("pdf4oereb-web-service",HttpStatus.OK);
    }
    
    @RequestMapping(value = "/convert", method = RequestMethod.POST)
    public ResponseEntity<?> convert(
            @RequestParam(name = "file", required = true)  MultipartFile multipartFile, 
            @RequestParam(name = "language", required = false, defaultValue="de") String language) {
     
        String filename = StringUtils.cleanPath(multipartFile.getOriginalFilename());
        
        // TODO
        // Funktionert das mit reverse proxy etc. ?
        if (multipartFile.getSize() == 0 || filename.trim().equalsIgnoreCase("") || filename == null) {
            log.warn("No file was uploaded. Redirecting to starting page.");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
            return new ResponseEntity<String>(headers, HttpStatus.FOUND);
        }

        try {
            Path tmpDirectory = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), FOLDER_PREFIX);
            Path uploadFilePath = Paths.get(tmpDirectory.toString(), filename);

            byte[] bytes = multipartFile.getBytes();
            Files.write(uploadFilePath, bytes);
            
            Locale locale = Locale.valueOf(language.toUpperCase());
            Converter converter =  new Converter();            
            File pdfFile = converter.runXml2Pdf(uploadFilePath.toFile().getAbsolutePath(), 
                    tmpDirectory.toFile().getAbsolutePath(), locale);
            
            InputStream is = new java.io.FileInputStream(pdfFile);
            return ResponseEntity
                    .ok().header("content-disposition", "attachment; filename=" + pdfFile.getName())
                    .contentLength(pdfFile.length())
                    .contentType(MediaType.APPLICATION_PDF).body(new InputStreamResource(is));                

        } catch (IOException | ConverterException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            return ResponseEntity.badRequest().contentType(MediaType.parseMediaType("text/plain")).body(sw.toString());
        }
    }

    @Scheduled(cron="0 * * * * *")
    private void cleanUp() {    
        java.io.File[] tmpDirs = new java.io.File(System.getProperty("java.io.tmpdir")).listFiles();
        if(tmpDirs!=null) {
            for (File tmpDir : tmpDirs) {
                if (tmpDir.getName().startsWith(FOLDER_PREFIX)) {
                    try {
                        FileTime creationTime = (FileTime) Files.getAttribute(Paths.get(tmpDir.getAbsolutePath()), "creationTime");                    
                        Instant now = Instant.now();
                        
                        long fileAge = now.getEpochSecond() - creationTime.toInstant().getEpochSecond();
                        if (fileAge > 60*60) {
                            log.info("deleting {}", tmpDir.getAbsolutePath());
                            FileSystemUtils.deleteRecursively(tmpDir);
                        }
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }
    }
}
