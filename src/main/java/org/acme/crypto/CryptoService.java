package org.acme.crypto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CryptoService {

    private static final long CACHE_TTL_SECONDS = 120; // 2 minutes

    @Inject
    @RestClient
    CoinGeckoClient coinGeckoClient;

    private final ConcurrentHashMap<String, CachedPrice> priceCache = new ConcurrentHashMap<>();

    private record CachedPrice(Map<String, Map<String, Number>> data, Instant fetchedAt) {
        boolean isExpired() {
            return Instant.now().isAfter(fetchedAt.plusSeconds(CACHE_TTL_SECONDS));
        }
    }

    private Map<String, Map<String, Number>> fetchPricesCached(String coinIds, String currencies) {
        String cacheKey = coinIds + "|" + currencies;
        CachedPrice cached = priceCache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.data;
        }
        Map<String, Map<String, Number>> data = coinGeckoClient.getPrice(coinIds, currencies);
        priceCache.put(cacheKey, new CachedPrice(data, Instant.now()));
        return data;
    }

    public CryptoPrice getCurrentPrice(String coinId, String currencies) {
        Map<String, Map<String, Number>> response = fetchPricesCached(coinId, currencies);
        Map<String, Number> prices = response.get(coinId);
        if (prices == null) {
            throw new IllegalArgumentException("Coin not found: " + coinId);
        }

        BigDecimal usd = prices.containsKey("usd")
                ? new BigDecimal(prices.get("usd").toString()) : null;
        BigDecimal brl = prices.containsKey("brl")
                ? new BigDecimal(prices.get("brl").toString()) : null;

        return new CryptoPrice(coinId, usd, brl);
    }

    public PortfolioValuation getPortfolioValuation() {
        List<CryptoHolding> holdings = CryptoHolding.listAll();
        if (holdings.isEmpty()) {
            return new PortfolioValuation(List.of(), BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // Collect unique coin IDs
        String coinIds = holdings.stream()
                .map(h -> h.coinId)
                .distinct()
                .collect(Collectors.joining(","));

        Map<String, Map<String, Number>> prices = fetchPricesCached(coinIds, "usd,brl");

        BigDecimal totalUsd = BigDecimal.ZERO;
        BigDecimal totalBrl = BigDecimal.ZERO;

        List<HoldingValuation> valuations = new java.util.ArrayList<>();
        for (CryptoHolding holding : holdings) {
            Map<String, Number> coinPrices = prices.get(holding.coinId);
            BigDecimal priceUsd = BigDecimal.ZERO;
            BigDecimal priceBrl = BigDecimal.ZERO;
            if (coinPrices != null) {
                priceUsd = coinPrices.containsKey("usd")
                        ? new BigDecimal(coinPrices.get("usd").toString()) : BigDecimal.ZERO;
                priceBrl = coinPrices.containsKey("brl")
                        ? new BigDecimal(coinPrices.get("brl").toString()) : BigDecimal.ZERO;
            }

            BigDecimal valueUsd = holding.quantity.multiply(priceUsd).setScale(2, RoundingMode.HALF_UP);
            BigDecimal valueBrl = holding.quantity.multiply(priceBrl).setScale(2, RoundingMode.HALF_UP);

            BigDecimal profitLossUsd = null;
            if (holding.averagePurchasePrice != null && "USD".equalsIgnoreCase(holding.purchaseCurrency)) {
                BigDecimal costBasis = holding.quantity.multiply(holding.averagePurchasePrice);
                profitLossUsd = valueUsd.subtract(costBasis).setScale(2, RoundingMode.HALF_UP);
            }

            totalUsd = totalUsd.add(valueUsd);
            totalBrl = totalBrl.add(valueBrl);

            valuations.add(new HoldingValuation(
                    holding.id, holding.coinId, holding.symbol, holding.quantity,
                    priceUsd, priceBrl, valueUsd, valueBrl, profitLossUsd));
        }

        return new PortfolioValuation(valuations, totalUsd, totalBrl);
    }

    public record CryptoPrice(String coinId, BigDecimal usd, BigDecimal brl) {}

    public record HoldingValuation(
            Long holdingId,
            String coinId,
            String symbol,
            BigDecimal quantity,
            BigDecimal currentPriceUsd,
            BigDecimal currentPriceBrl,
            BigDecimal totalValueUsd,
            BigDecimal totalValueBrl,
            BigDecimal profitLossUsd) {}

    public record PortfolioValuation(
            List<HoldingValuation> holdings,
            BigDecimal totalValueUsd,
            BigDecimal totalValueBrl) {}
}
