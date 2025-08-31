package codeforces.codeSherpa.Config;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Component
public class PromptLoader {
    private final ResourceLoader resourceLoader;

    public PromptLoader(ResourceLoader resourceLoader){
        this.resourceLoader = resourceLoader;
    }
    public String loadPromptFile(String nameOfFile) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:prompts/"+nameOfFile);
        try{
            Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
            return FileCopyUtils.copyToString(reader);
        }
        catch (Exception e){
            throw new IOException("Error in Input/ Output Operation");
        }
    }
}
