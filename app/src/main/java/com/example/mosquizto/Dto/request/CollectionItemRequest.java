package com.example.mosquizto.Dto.request;

/**
 * DTO đại diện cho một thẻ (Term/Definition) khi gửi lên Server
 */
public class CollectionItemRequest {
    private String term;
    private String definition;

    public CollectionItemRequest(String term, String definition) {
        this.term = term;
        this.definition = definition;
    }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }
}