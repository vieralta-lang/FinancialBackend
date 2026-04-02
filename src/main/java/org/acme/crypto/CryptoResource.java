package org.acme.crypto;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("/crypto")
@Tag(name = "Crypto Portfolio", description = "Track cryptocurrency holdings and get real-time prices via CoinGecko")
public class CryptoResource {

    @Inject
    CryptoService cryptoService;

    @GET
    @Path("/holdings")
    @Operation(summary = "List all crypto holdings")
    public List<CryptoHolding> listHoldings() {
        return CryptoHolding.listAll();
    }

    @GET
    @Path("/holdings/{id}")
    @Operation(summary = "Get a crypto holding by ID")
    @APIResponse(responseCode = "404", description = "Holding not found")
    public CryptoHolding getHolding(@Parameter(description = "Holding ID") @PathParam("id") Long id) {
        CryptoHolding holding = CryptoHolding.findById(id);
        if (holding == null) {
            throw new WebApplicationException("Crypto holding not found", 404);
        }
        return holding;
    }

    @POST
    @Path("/holdings")
    @Transactional
    @Operation(summary = "Add a crypto holding", description = "Register your crypto quantity, e.g. 0.5 BTC")
    @APIResponse(responseCode = "201", description = "Holding created")
    public Response createHolding(@Valid CryptoHolding holding) {
        holding.persist();
        return Response.status(Response.Status.CREATED).entity(holding).build();
    }

    @PUT
    @Path("/holdings/{id}")
    @Transactional
    @Operation(summary = "Update a crypto holding")
    @APIResponse(responseCode = "404", description = "Holding not found")
    public CryptoHolding updateHolding(
            @Parameter(description = "Holding ID") @PathParam("id") Long id,
            @Valid CryptoHolding updated) {
        CryptoHolding holding = CryptoHolding.findById(id);
        if (holding == null) {
            throw new WebApplicationException("Crypto holding not found", 404);
        }
        holding.coinId = updated.coinId;
        holding.symbol = updated.symbol;
        holding.quantity = updated.quantity;
        holding.averagePurchasePrice = updated.averagePurchasePrice;
        holding.purchaseCurrency = updated.purchaseCurrency;
        holding.notes = updated.notes;
        holding.updatedAt = LocalDateTime.now();
        return holding;
    }

    @DELETE
    @Path("/holdings/{id}")
    @Transactional
    @Operation(summary = "Delete a crypto holding")
    @APIResponse(responseCode = "204", description = "Holding deleted")
    public Response deleteHolding(@Parameter(description = "Holding ID") @PathParam("id") Long id) {
        CryptoHolding holding = CryptoHolding.findById(id);
        if (holding == null) {
            throw new WebApplicationException("Crypto holding not found", 404);
        }
        holding.delete();
        return Response.noContent().build();
    }

    @GET
    @Path("/price")
    @Operation(summary = "Get current crypto price",
            description = "Fetches real-time price from CoinGecko. Default coin: bitcoin. Returns USD and BRL.")
    public Response getPrice(
            @Parameter(description = "CoinGecko coin ID (e.g. bitcoin, ethereum, solana)")
            @QueryParam("coinId") String coinId) {
        String id = (coinId != null && !coinId.isBlank()) ? coinId : "bitcoin";
        try {
            return Response.ok(cryptoService.getCurrentPrice(id, "usd,brl")).build();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                return Response.status(429).entity("Rate limit exceeded. Try again in a few minutes.").build();
            }
            throw e;
        }
    }

    @GET
    @Path("/portfolio")
    @Operation(summary = "Get portfolio valuation",
            description = "Returns current value of all holdings in USD and BRL with profit/loss calculation")
    public Response getPortfolio() {
        try {
            return Response.ok(cryptoService.getPortfolioValuation()).build();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("429")) {
                return Response.status(429).entity("Rate limit exceeded. Try again in a few minutes.").build();
            }
            throw e;
        }
    }
}
