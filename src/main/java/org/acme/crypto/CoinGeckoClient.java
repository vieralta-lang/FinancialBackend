package org.acme.crypto;

import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@RegisterRestClient(configKey = "coingecko-api")
public interface CoinGeckoClient {

    @GET
    @Path("/simple/price")
    Map<String, Map<String, Number>> getPrice(
            @QueryParam("ids") String ids,
            @QueryParam("vs_currencies") String vsCurrencies);
}
