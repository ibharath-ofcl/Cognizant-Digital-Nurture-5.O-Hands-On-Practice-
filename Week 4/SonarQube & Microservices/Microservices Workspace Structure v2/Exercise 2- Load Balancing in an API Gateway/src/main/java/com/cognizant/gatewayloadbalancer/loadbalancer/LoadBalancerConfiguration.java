package com.cognizant.gatewayloadbalancer.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Custom Spring Cloud LoadBalancer Configuration.
 * Instantiates the custom Random Load Balancer using ReactorLoadBalancer.
 * Note: This configuration class should not be annotated with @Configuration if registered 
 * via @LoadBalancerClients(defaultConfiguration = LoadBalancerConfiguration.class) in the main class
 * to avoid context pollution.
 */
public class LoadBalancerConfiguration {

    /**
     * Creates a custom ReactorLoadBalancer bean for each load-balanced client.
     *
     * @param environment the Spring Environment to extract the service name
     * @param loadBalancerClientFactory factory providing instances of ServiceInstanceListSupplier
     * @return ReactorLoadBalancer configured with CustomRandomLoadBalancer
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> customRandomLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        // Retrieve the service name for the current load balancer context
        String serviceName = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        
        return new CustomRandomLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(serviceName, ServiceInstanceListSupplier.class),
                serviceName);
    }
}
