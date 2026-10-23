package com.certech.quote;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
@RestController
@RequestMapping("/api/cotizaciones")
public class QuoteController {
 private final QuoteService service;
 public QuoteController(QuoteService service){this.service=service;}
 @PostMapping public ResponseEntity<QuoteResponse> create(@Valid @RequestBody QuoteRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));}
}
