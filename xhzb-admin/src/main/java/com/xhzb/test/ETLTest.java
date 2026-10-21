package com.xhzb.test;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.InputStreamResource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

@SpringBootTest
public class ETLTest {

    @Autowired
    private VectorStore vectorStore;

    @Test
    public void readTest() throws FileNotFoundException {

        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream("D:\\abc.txt"));

        TextReader textReader = new TextReader(inputStreamResource);

        textReader.getCustomMetadata().put("filename","abc.txt");
        textReader.getCustomMetadata().put("type","txt");

        List<Document> documents = textReader.read();
        documents.forEach(System.out::println);
    }

    @Test
    public void readPDFByPage() throws FileNotFoundException {
        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream("D:\\itheima-project\\护理员工工作手册.pdf"));

        PagePdfDocumentReader pagePdfDocumentReader = new PagePdfDocumentReader(inputStreamResource,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build()
                );

        TextSplitter textSplitter = new TokenTextSplitter();
        System.out.println("分割之前的文档数:"+pagePdfDocumentReader.read().size());
        List<Document> documents = textSplitter.apply(pagePdfDocumentReader.read());
        System.out.println("分割之后的文档数:"+documents.size());
        System.out.println(documents);

        vectorStore.add(documents);
    }
    @Test
    public void readPDFByParagraph() throws FileNotFoundException {
        InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream("D:\\itheima-project\\护理员工工作手册.pdf"));

        ParagraphPdfDocumentReader paragraphPdfDocumentReader = new ParagraphPdfDocumentReader(inputStreamResource,
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build()
                );

        System.out.println(paragraphPdfDocumentReader.read());
    }

    @Test
    public void retrievalTest(){
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query("护理服务宗旨与核心价值是什么?")
                .similarityThreshold(0.5)
                .topK(5)
                .build());

        for (Document document : documents) {
            System.out.println(document.getMetadata());
        }

    }
}
