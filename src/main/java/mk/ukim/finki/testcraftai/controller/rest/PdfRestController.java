package mk.ukim.finki.testcraftai.controller.rest;

import lombok.AllArgsConstructor;
import mk.ukim.finki.testcraftai.service.PdfService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PdfRestController {
    private final PdfService pdfService;

    @GetMapping("/api/teacher/quizzes/{quizId}/pdf")
    public ResponseEntity<byte[]> exportQuizPdf(@PathVariable Long quizId) {
        byte[] pdfBytes = pdfService.generateQuizPdf(quizId);

        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("quiz_" + quizId + "_export.pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}
