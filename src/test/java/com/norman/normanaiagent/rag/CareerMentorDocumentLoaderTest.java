package com.norman.normanaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class CareerMentorDocumentLoaderTest {

    @Resource
    private CareerMentorDocumentLoader careerMentorDocumentLoader;

    @Test
    void loadMarkdowns() {
        List<Document> documents = careerMentorDocumentLoader.loadMarkdowns();
        Assertions.assertNotNull(documents);
        Assertions.assertFalse(documents.isEmpty());
    }
}
