package pl.pw.edu.po.search_engine.simplesearchengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DocumentRequest {
    @JsonProperty("id")
    private String id;

    @JsonProperty("content")
    private String content;

    @JsonProperty("title")
    private String title;

    @JsonProperty("url")
    private String url;

    public DocumentRequest(String id, String content) {
        this.id = id;
        this.content = content;
    }
}
