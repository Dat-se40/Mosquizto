package com.example.mosquizto.Models;

public class Flashcard {

    public static final int STATUS_UNSEEN  = 0;
    public static final int STATUS_KNOWN   = 1;
    public static final int STATUS_LEARNING = 2;

    private String term;
    private String definition;
    private int    status;
    private boolean starred;

    // ── Constructors ──────────────────────────────────────────────────────────

    public Flashcard() {
        this.status  = STATUS_UNSEEN;
        this.starred = false;
    }

    public Flashcard(String term, String definition) {
        this.term       = term;
        this.definition = definition;
        this.status     = STATUS_UNSEEN;
        this.starred    = false;
    }

    public Flashcard(String term, String definition, int status, boolean starred) {
        this.term       = term;
        this.definition = definition;
        this.status     = status;
        this.starred    = starred;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public boolean isStarred() { return starred; }
    public void setStarred(boolean starred) { this.starred = starred; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public boolean isKnown()    { return status == STATUS_KNOWN; }
    public boolean isLearning() { return status == STATUS_LEARNING; }
    public boolean isUnseen()   { return status == STATUS_UNSEEN; }

    public void reset() {
        this.status = STATUS_UNSEEN;
    }

    @Override
    public String toString() {
        return "Flashcard{term='" + term + "', definition='" + definition
                + "', status=" + status + ", starred=" + starred + "}";
    }
}