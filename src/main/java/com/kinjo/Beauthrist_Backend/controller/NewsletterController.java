package com.kinjo.Beauthrist_Backend.controller;

import com.kinjo.Beauthrist_Backend.dto.Response;
import com.kinjo.Beauthrist_Backend.entity.Product;
import com.kinjo.Beauthrist_Backend.exception.NotFoundException;
import com.kinjo.Beauthrist_Backend.exception.UserAlreadyExistsException;
import com.kinjo.Beauthrist_Backend.repository.ProductRepo;
import com.kinjo.Beauthrist_Backend.service.interf.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/newsletter")
@RequiredArgsConstructor
public class NewsletterController {

    private final NewsletterService newsletterService;
    private final ProductRepo productRepo;

    @PostMapping("/subscribe")
    public ResponseEntity<Response> subscribe(@RequestParam(required = true) String email) {
        try {
            return ResponseEntity.ok(newsletterService.subscribe(email));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(400).body(
                    Response.builder()
                            .status(400)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Response.builder()
                            .status(500)
                            .message("An unexpected error occurred: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<Response> unsubscribe(@RequestParam(required = true) String email) {
        try {
            return ResponseEntity.ok(newsletterService.unsubscribe(email));
        } catch (NotFoundException e) {
            return ResponseEntity.status(404).body(
                    Response.builder()
                            .status(404)
                            .message(e.getMessage())
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Response.builder()
                            .status(500)
                            .message("An unexpected error occurred: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/subscribers")
    public ResponseEntity<Response> getAllSubscribers() {
        return ResponseEntity.ok(newsletterService.getAllSubscribers());
    }

}
