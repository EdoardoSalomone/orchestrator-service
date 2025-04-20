package it.edoardo.orchestratorservice.service;

import it.edoardo.orchestratorservice.dto.GenericResponse;
import it.edoardo.orchestratorservice.dto.OrderItemDTO;
import it.edoardo.orchestratorservice.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private final WebClient.Builder webClientBuilder;
    //private final DiscoveryClient discoveryClient;

    public Mono<String> processOrder(OrderRequest order) {
        List<Integer> ids = order.getItems().stream()
                .map(OrderItemDTO::getProductId)
                .toList();
        return getProductNameByIds(ids)
                .flatMap(nameMap -> {
                    order.getItems().forEach(item ->
                            item.setProductName(nameMap.getOrDefault(item.getProductId(), "sconosciuto"))
                    );
        return callOrderService(order)
                .flatMap(orderResponse -> {
                    if(!"SUCCESS".equals(orderResponse.getStatus())) {
                        return Mono.just("Ordine non salvato: " + orderResponse.getMessage());
                    }

                    return callInventoryService(order)
                            .map(inventoryResponse ->{
                                if(!"SUCCESS".equals(inventoryResponse.getStatus())) {
                                    return "Errore Inventario: " + inventoryResponse.getMessage();
                                }
                                return "Ordine completato con successo";
                            });
                });
                });

    }

    public Mono<Map<Integer,String>> getProductNameByIds(List<Integer> productIds) {
        return webClientBuilder.build()
                .post()
                .uri("http://inventory-service/inventory/product/names")
                .bodyValue(productIds)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<Integer,String>>() {});
    }

    private Mono<GenericResponse> callOrderService(OrderRequest order) {
        return webClientBuilder.build()
                .post()
                .uri("http://order-service/orders")
                .bodyValue(order)
                .retrieve()
                .bodyToMono(GenericResponse.class);
    }

    private Mono<GenericResponse> callInventoryService(OrderRequest order) {
        return webClientBuilder.build()
                .post()
                .uri("http://inventory-service/inventory/decrease")
                .bodyValue(order)
                .retrieve()
                .bodyToMono(GenericResponse.class);
    }

    private Mono<GenericResponse> callNotificationService(OrderRequest order) {
        return webClientBuilder.build()
                .post()
                .uri("http://notification-service/notify")
                .bodyValue(order)
                .retrieve()
                .bodyToMono(GenericResponse.class);
    }

}
