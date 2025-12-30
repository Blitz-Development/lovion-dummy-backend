package nl.blitz.loviondummy.soap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

@Component
public class SoapFaultSimulator {

    @Value("${soap.fault.simulation.enabled:false}")
    private boolean enabled;

    @Value("${soap.fault.simulation.probability:0.1}")
    private double probability;

    private final Random random = new Random();
    private int requestCount = 0;

    @Value("${soap.fault.rate-limit.max-requests:10}")
    private int maxRequests;

    @Value("${soap.fault.rate-limit.window-seconds:60}")
    private int windowSeconds;

    private LocalDateTime windowStart = LocalDateTime.now();

    /**
     * Roept je aan vanuit de endpoint.
     * - Geeft null terug als er géén fout gesimuleerd wordt.
     * - Geeft een SoapFaultException terug als we een fault willen gooien.
     */


    public SoapFaultException simulateFault() {
        if (!enabled) {
            return null;
        }

        // check of SoapFaultSimulator wel wordt aangeroepen...
        System.out.println(">>> simulateFault() CALLED | enabled=" + enabled + " prob=" + probability
                + " maxRequests=" + maxRequests + " windowSeconds=" + windowSeconds);


        // 1. Rate limit check
        if (isRateLimitExceeded()) {
            return new SoapFaultException(
                    "SOAP-ENV:Server",
                    "Rate limit exceeded",
                    "Maximum " + maxRequests + " requests per " + windowSeconds + " seconds",
                    true // transient
            );
        }

        // 2. Random fault op basis van probability
        if (random.nextDouble() < probability) {
            return generateRandomFault();
        }

        // 3. Geen fout → null
        return null;
    }

    private boolean isRateLimitExceeded() {
        LocalDateTime now = LocalDateTime.now();

        if (Duration.between(windowStart, now).getSeconds() > windowSeconds) {
            // Nieuw tijdvenster starten
            windowStart = now;
            requestCount = 0;
        }

        requestCount++;
        return requestCount > maxRequests;
    }

    private SoapFaultException generateRandomFault() {
        int faultType = random.nextInt(3);

        switch (faultType) {
            case 0:
                // Service tijdelijk niet beschikbaar (transient)
                return new SoapFaultException(
                        "SOAP-ENV:Server",
                        "Service temporarily unavailable",
                        "The service is currently overloaded. Please retry later.",
                        true
                );
            case 1:
                // Foute credentials (permanent)
                return new SoapFaultException(
                        "SOAP-ENV:Client",
                        "Invalid credentials",
                        "Authentication failed. Please check your credentials.",
                        false
                );
            case 2:
                // Rate limit exceeded (transient, andere tekst)
                return new SoapFaultException(
                        "SOAP-ENV:Server",
                        "Rate limit exceeded",
                        "Too many requests. Retry after 30 seconds.",
                        true
                );
            default:
                return null;
        }
    }
}
