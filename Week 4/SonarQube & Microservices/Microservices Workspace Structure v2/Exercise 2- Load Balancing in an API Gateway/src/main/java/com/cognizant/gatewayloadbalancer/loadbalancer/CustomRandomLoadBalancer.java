package com.cognizant.gatewayloadbalancer.loadbalancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Custom Random Load Balancer implementing ReactorServiceInstanceLoadBalancer.
 * Picks a random instance from the list of available service instances supplied by the ServiceInstanceListSupplier.
 */
public class CustomRandomLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private static final Logger log = LoggerFactory.getLogger(CustomRandomLoadBalancer.class);

    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;
    private final String serviceId;

    /**
     * Constructor for CustomRandomLoadBalancer.
     *
     * @param serviceInstanceListSupplierProvider the provider for ServiceInstanceListSupplier
     * @param serviceId the name of the service to load balance
     */
    public CustomRandomLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
                                    String serviceId) {
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.serviceId = serviceId;
    }

    /**
     * Chooses a service instance randomly from the supplier list.
     *
     * @param request the loadbalancer request context
     * @return a Mono wrapping the selected ServiceInstance response
     */
    @Override
    @SuppressWarnings("rawtypes")
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServiceInstanceListSupplier supplier = this.serviceInstanceListSupplierProvider.getIfAvailable();
        if (supplier == null) {
            log.warn("No ServiceInstanceListSupplier found for service: {}", serviceId);
            return Mono.just(new EmptyResponse());
        }
        return supplier.get(request).next().map(this::selectRandomInstance);
    }

    /**
     * Selects a random instance from the list of service instances.
     *
     * @param instances list of available ServiceInstance
     * @return Response containing the selected instance
     */
    private Response<ServiceInstance> selectRandomInstance(List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            log.warn("No instances available for service: {}", serviceId);
            return new EmptyResponse();
        }

        // Random Selection Strategy
        int index = ThreadLocalRandom.current().nextInt(instances.size());
        ServiceInstance selectedInstance = instances.get(index);

        log.info("Custom Random Load Balancer [Service: {}] -> Selected instance: {} (URI: {})",
                serviceId, selectedInstance.getInstanceId(), selectedInstance.getUri());

        return new DefaultResponse(selectedInstance);
    }
}
