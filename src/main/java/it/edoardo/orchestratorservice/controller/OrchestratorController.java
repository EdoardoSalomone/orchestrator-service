package it.edoardo.orchestratorservice.controller;

import com.netflix.discovery.DiscoveryClient;
import it.edoardo.orchestratorservice.dto.OrderRequest;
import it.edoardo.orchestratorservice.service.OrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class OrchestratorController {

    private final OrchestratorService orchestratorService;

    @PostMapping("/order")
    public Mono<ResponseEntity<String>> createOrder (@RequestBody OrderRequest order) {
        return orchestratorService.processOrder(order)
                .map(result -> ResponseEntity.ok(result))
                .onErrorResume(e -> Mono.just(ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Errore interno" + e.getMessage())));
    }

    @PostMapping("/productNames")
    public Mono<ResponseEntity<Map<Integer,String>>> getProductNames (@RequestBody List<Integer> productIds) {
      return orchestratorService.getProductNameByIds(productIds)
              .map(result -> ResponseEntity.ok(result))
              .onErrorResume(e -> Mono.just(ResponseEntity
                      .status(HttpStatus.INTERNAL_SERVER_ERROR)
                      .body(Collections.emptyMap())));
    }
}
