package com.plantpal.logic;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class WissensdatenbankService {
    private static final String KNOWLEDGE_BASE_FILE = "/json/wissensdatenbank.json";

    // Methode zum Laden der Einträge aus der JSON-Datei
    public List<KnowledgeBaseEntry> loadKnowledgeBase() {
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(KNOWLEDGE_BASE_FILE)))) {
            Gson gson = new Gson();
            Type knowledgeBaseListType = new TypeToken<List<KnowledgeBaseEntry>>() {}.getType();
            return gson.fromJson(reader, knowledgeBaseListType);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Klasse für einen Eintrag in der Wissensdatenbank
    public static class KnowledgeBaseEntry {
        private int knowledge_id;
        private String name;
        private String identification;
        private String control;
        private String additional_info;
        private String imagePath;
        private String imageAuthor;

        // Getter und Setter
        public int getKnowledge_id() { return knowledge_id; }
        public String getName() { return name; }
        public String getIdentification() { return identification; }
        public String getControl() { return control; }
        public String getAdditional_info() { return additional_info; }
        public String getImagePath() { return imagePath; }
        public String getImageAuthor() {
            return imageAuthor;
        }

        public void setImageAuthor(String imageAuthor) {
            this.imageAuthor = imageAuthor;
        }
    }
}
